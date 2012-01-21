/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.android.core.messages;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.frostwire.android.core.Constants;
import com.frostwire.android.util.ByteUtils;

/**
 * PingMessages are sent via Multicast and/or Broadcast periodically.
 * 
 * The ping message has the following structure:
 * - Listening Port (2 bytes)
 * - Number of files shared (3 bytes)
 * - Nickname (16 bytes)
 * - bye (1 byte) - If !=0 that peer is disconnecting.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class PingMessage extends FrostWireMessage {

    private int listeningPort;
    private int numSharedFiles;
    private String nickname;
    private boolean bye;

    public PingMessage(InetAddress origin, byte[] data) {
        super(origin, data);
    }

    public PingMessage(int listeningPort, int numSharedFiles, String nickname, boolean bye) {
        super(Constants.MESSAGE_TYPE_PING);

        this.listeningPort = listeningPort;
        this.numSharedFiles = numSharedFiles;
        this.nickname = nickname;
        this.bye = bye;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public int getNumSharedFiles() {
        return numSharedFiles;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean getBye() {
        return bye;
    }

    @Override
    public void fromBytes(byte[] data) {
        super.fromBytes(data); // so it loads headers.

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(Constants.FW_HEADER_SIZE);

        // port 2 bytes
        byte[] port = new byte[2];
        buffer.get(port);
        this.listeningPort = ByteUtils.byteArrayToSmallInt(port, 0);

        // shared files 3 bytes
        byte[] shared = new byte[3];
        buffer.get(shared);
        this.numSharedFiles = ByteUtils.tripleByteArrayToSmallInt(shared, 0);

        // nickname - 16 bytes
        byte[] nickname = new byte[16];
        buffer.get(nickname);
        this.nickname = new String(nickname).trim();
        // is saying good bye
        bye = buffer.get() != (byte) 0;

        getHeader().setPayloadSize(38); // 2+3+16+8+8+1;
    }

    /**
     * Updates the payloadBytes and returns this message's payload in bytes.
     * 
     * @return a byte array containing only the payload of the PingMessage. No
     *         Message headers are returned.
     */
    @Override
    public byte[] getPayloadBytes() {

        ByteBuffer buffer = ByteBuffer.allocate(22);

        // port - 2 bytes
        buffer.put(ByteUtils.smallIntToByteArray(getListeningPort()));

        // shared files - 3 bytes
        buffer.put(ByteUtils.smallIntToTripleByteArray(getNumSharedFiles()));

        // nickname - 16 bytes (we pad to the right with 0)
        String nick = getNickname();
        byte[] nickname = new byte[16];

        if (nick.length() > 16) {
            // we clip if too long
            System.arraycopy(nick.substring(0, 16).getBytes(), 0, nickname, 0, 16);
        } else {
            System.arraycopy(nick.getBytes(), 0, nickname, 0, nick.length());
        }

        buffer.put(nickname);

        // bye - 1 byte
        buffer.put(getBye() ? (byte) 1 : (byte) 0);

        buffer.rewind();

        return buffer.array();
    }
}
