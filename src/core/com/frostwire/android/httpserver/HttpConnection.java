/*
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * encapsulates all the connection specific state for a HTTP connection
 * one of these is hung from the selector attachment and is used to locate
 * everything from that.
 */
public class HttpConnection {

    @SuppressWarnings("unused")
    private static final String TAG = "FW.HttpConnection";

    HttpContext context;

    /* high level streams returned to application */
    InputStream i;

    /* low level stream that sits directly over channel */
    InputStream raw;
    OutputStream rawout;

    SocketChannel chan;
    SelectionKey selectionKey;
    String protocol;
    long time;
    int remaining;
    boolean closed = false;

    public HttpConnection() {
    }

    public String toString() {
        String s = null;
        if (chan != null) {
            s = chan.toString();
        }
        return s;
    }

    void setChannel(SocketChannel c) {
        chan = c;
    }

    void setContext(HttpContext ctx) {
        context = ctx;
    }

    void setParameters(InputStream in, OutputStream rawout, SocketChannel chan, String protocol, HttpContext context, InputStream raw) {
        this.context = context;
        this.i = in;
        this.rawout = rawout;
        this.raw = raw;
        this.protocol = protocol;
        this.chan = chan;
    }

    SocketChannel getChannel() {
        return chan;
    }

    synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;

        if (chan != null) {
            //Log.d(TAG, "Closing connection: " + chan.toString());
        }

        if (!chan.isOpen()) {
            HttpServer.dprint("Channel already closed");
            return;
        }
        try {
            /* need to ensure temporary selectors are closed */
            if (raw != null) {
                raw.close();
            }
        } catch (IOException e) {
            HttpServer.dprint(e);
        }
        try {
            if (rawout != null) {
                rawout.close();
            }
        } catch (IOException e) {
            HttpServer.dprint(e);
        }
        try {
            chan.close();
        } catch (IOException e) {
            HttpServer.dprint(e);
        }
    }

    /* remaining is the number of bytes left on the lowest level inputstream
     * after the exchange is finished
     */
    void setRemaining(int r) {
        remaining = r;
    }

    int getRemaining() {
        return remaining;
    }

    SelectionKey getSelectionKey() {
        return selectionKey;
    }

    InputStream getInputStream() {
        return i;
    }

    OutputStream getRawOutputStream() {
        return rawout;
    }

    String getProtocol() {
        return protocol;
    }

    HttpContext getHttpContext() {
        return context;
    }
}
