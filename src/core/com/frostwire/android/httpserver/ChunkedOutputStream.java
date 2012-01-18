/*
 * Copyright (c) 2005, 2007, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.frostwire.android.httpserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A class which allows the caller to write an arbitrary
 * number of bytes to an underlying stream.
 * normal close() does not close the underlying stream
 *
 * This class is buffered.
 *
 * Each chunk is written in one go as:
 * 
 * abcd\r\nxxxxxxxxxxxxxx\r\n
 *
 * abcd is the chunk-size, and xxx is the chunk data
 * If the length is less than 4 chars (in size) then the buffer
 * is written with an offset.
 * Final chunk is:
 * 0\r\n\r\n
 */
public class ChunkedOutputStream extends FilterOutputStream {

    /* max. amount of user data per chunk */
    private static final int CHUNK_SIZE = 4096;

    /* allow 4 bytes for chunk-size plus 4 for CRLFs */
    private static final int OFFSET = 6; /* initial <=4 bytes for len + CRLF */

    private boolean _closed = false;
    private int pos = OFFSET;
    private int count = 0;
    private byte[] buf = new byte[CHUNK_SIZE + OFFSET + 2];
    private HttpExchange _exchange;

    public ChunkedOutputStream(HttpExchange exchange, OutputStream src) {
        super(src);
        _exchange = exchange;
    }

    public void write(int b) throws IOException {
        if (_closed) {
            throw new StreamClosedException();
        }
        buf[pos++] = (byte) b;
        count++;
        if (count == CHUNK_SIZE) {
            writeChunk();
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (_closed) {
            throw new StreamClosedException();
        }
        int remain = CHUNK_SIZE - count;
        if (len > remain) {
            System.arraycopy(b, off, buf, pos, remain);
            count = CHUNK_SIZE;
            writeChunk();
            len -= remain;
            off += remain;
            while (len > CHUNK_SIZE) {
                System.arraycopy(b, off, buf, OFFSET, CHUNK_SIZE);
                len -= CHUNK_SIZE;
                off += CHUNK_SIZE;
                count = CHUNK_SIZE;
                writeChunk();
            }
            pos = OFFSET;
        }
        if (len > 0) {
            System.arraycopy(b, off, buf, pos, len);
            count += len;
            pos += len;
        }
    }

    /**
     * write out a chunk , and reset the pointers
     * chunk does not have to be CHUNK_SIZE bytes
     * count must == number of user bytes (<= CHUNK_SIZE)
     */
    private void writeChunk() throws IOException {
        char[] c = Integer.toHexString(count).toCharArray();
        int clen = c.length;
        int startByte = 4 - clen;
        int i;
        for (i = 0; i < clen; i++) {
            buf[startByte + i] = (byte) c[i];
        }
        buf[startByte + (i++)] = '\r';
        buf[startByte + (i++)] = '\n';
        buf[startByte + (i++) + count] = '\r';
        buf[startByte + (i++) + count] = '\n';
        out.write(buf, startByte, i + count);
        count = 0;
        pos = OFFSET;
    }

    public void close() throws IOException {
        if (_closed) {
            return;
        }
        flush();
        try {
            /* write an empty chunk */
            writeChunk();
            out.flush();
            LeftOverInputStream is = _exchange.getOriginalInputStream();
            if (!is.isClosed()) {
                is.close();
            }
            /* some clients close the connection before empty chunk is sent */
        } catch (IOException e) {

        } finally {
            _closed = true;
        }

        WriteFinishedEvent e = new WriteFinishedEvent(_exchange);
        _exchange.getHttpContext().getServer().addEvent(e);
    }

    public void flush() throws IOException {
        if (_closed) {
            throw new StreamClosedException();
        }
        if (count > 0) {
            writeChunk();
        }
        out.flush();
    }
}
