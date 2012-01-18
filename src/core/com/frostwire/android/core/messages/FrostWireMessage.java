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

import com.frostwire.android.core.Constants;
import com.frostwire.android.util.ByteUtils;

/**
 * A FrostWire Message. To be sent or received between peers.
 * It's made of a FrostWireMessageHeader, and a byte[] payload.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class FrostWireMessage {

    /* Where this message came from, usually the InetAddress from the socket*/
    private final InetAddress address;
    private final FrostWireMessageHeader header;
    private byte[] payload;

    public FrostWireMessage(byte messageType) {
        this.address = null;
        this.header = new FrostWireMessageHeader();

        header.setType(messageType);
    }

    public FrostWireMessage(InetAddress origin, byte[] data) {
        this.address = origin;
        this.header = new FrostWireMessageHeader();

        fromBytes(data); //fills up the header and the payload.
    }

    public InetAddress getAddress() {
        return address;
    }

    /**
     * Gets the FrostWireMessageHeader object for this message.
     */
    public FrostWireMessageHeader getHeader() {
        return header;
    }

    public byte[] getPayloadBytes() {
        return payload;
    }

    /**
     * Returns the type of the message.
     * 
     * @see FrostWireMessageHeader 
     */
    public byte getType() {
        return header.getType();
    }

    public byte[] getUUID() {
        return header.getUUID();
    }

    public void setUUID(byte[] uuid) {
        header.setUUID(uuid);
    }

    /**
     * Returns a complete FrostWireMessage in bytes, including it's FrostWireMessageHeader bytes.
     * 
     * This will make sure the header is consistent and up to date with the payload.
     * 
     */
    public byte[] toBytes() {

        payload = getPayloadBytes(); // in case there is an override

        updateMessageHeader();

        byte[] headerBytes = header.toBytes();

        // for empty messages
        if (payload == null) {
            return headerBytes;
        }

        return ByteUtils.appendByteArrays(headerBytes, payload);
    }

    /**
     * Given a byte array that represents a full frostwire message,
     * it will return a corresponding FrostWireMessage Object.
     */
    public void fromBytes(byte[] data) {
        // initialize the headers.
        byte[] headerBytes = new byte[Constants.FW_HEADER_SIZE];
        System.arraycopy(data, 0, headerBytes, 0, Constants.FW_HEADER_SIZE);
        header.fromBytes(headerBytes);

        payload = new byte[header.getPayloadSize()];
        System.arraycopy(data, Constants.FW_HEADER_SIZE, payload, 0, payload.length);
    }

    /**
     * Updates the payload size and payload checksum fields of the message header
     * using this messages's byte content. 
     */
    private void updateMessageHeader() {
        if (header.getClientIdentifier() == null) {
            header.setClientIdentifier(Constants.CLIENT_IDENTIFIER);
        }

        if (header.getClientVersion() == null) {
            header.setClientVersion(Constants.FROSTWIRE_VERSION);
        }

        if (payload != null) {
            header.setPayloadSize(payload.length);
            header.setPayloadChecksum(ByteUtils.getByteArrayChecksum(payload));
        } else {
            header.setPayloadSize(0);
            header.setPayloadChecksum(ByteUtils.getByteArrayChecksum(new byte[] { 0, 0 }));
        }
    }
}