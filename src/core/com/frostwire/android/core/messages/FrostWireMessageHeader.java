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

import java.nio.ByteBuffer;

import com.frostwire.android.core.Constants;
import com.frostwire.android.util.ByteUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class FrostWireMessageHeader {

    /**
     * 1 byte field to say what kind of message this is.
     */
    private byte type;

    /**
     * 1 byte Version field.
     */
    private byte protocolVersion;

    /**
     * 2 byte client id - 2 characters, we're "FW".
     */
    private byte[] clientIdentifier = Constants.CLIENT_IDENTIFIER;

    /**
     * 3 byte client version - mayor[0], minor[1], revision[2].
     */
    private byte[] clientVersion = Constants.FROSTWIRE_VERSION;

    /**
     * 16 bytes (128bit - RFC- UUID identifier letting us know who is the sender.
     */
    private byte[] uuid;

    /**
     * 2 bytes Size of the payload (doesn't include this message header, whose size is constant
     */
    private byte[] payloadSize;

    /**
     * 3 bytes Checksum of the payload
     */
    private byte[] payloadChecksum;

    /**
     * Returns this header in bytes 
     *   
     *   2   - FW     = frostwire message start
     *   1   - PV      = protocol version
     *   2   - CID    = client identifier "FW"
     *   3   - CV     = client version X.Y.Z
     *   1   - MT     = message type
     *   16 - UUID  = universal user identifier
     *   2   - PSZ    = payload size
     *   3   - CHK   = payload checksum
     * */
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.FW_HEADER_SIZE); //2 + 1 + 1 + 16 + 2 + 3 = 25;

        buffer.put(Constants.MESSAGE_HEADER_FIELD_FW); //2 - FW
        buffer.put(Constants.FROSTWIRE_PROTOCOL_VERSION); //1 - PV
        buffer.put(getClientIdentifier()); //2 - CID
        buffer.put(getClientVersion()); // 3 - CV
        buffer.put(getType()); //1 - MT
        buffer.put(getUUID()); //16 - UUID
        buffer.put(getPayloadSizeInBytes()); //2 - PSZ
        buffer.put(getPayloadChecksum()); //3 -CHK

        return buffer.array();
    }

    public void fromBytes(byte[] data) {
        if (data == null || data.length < Constants.FW_HEADER_SIZE || data[0] != 0x46 || data[1] != 0x57) {
            throw new IllegalArgumentException("can't create frostwire message with the given data.");
        }

        /*
         *   2   - FW     = frostwire message start
         *   1   - PV      = protocol version
         *   2   - CID    = client identifier "FW"
         *   3   - CV     = client version X.Y.Z
         *   1   - MT     = message type
         *   16 - UUID  = universal user identifier
         *   2   - PSZ    = payload size
         *   3   - CHK   = payload checksum
        */

        // FW - 2
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(0);
        buffer.get();
        buffer.get(); //skip FW - 2

        // PV - 1
        setProtocolVersion(buffer.get()); //1

        // CID - 2
        byte[] clientIdentifier = new byte[2];
        buffer.get(clientIdentifier); //2
        setClientIdentifier(clientIdentifier);

        // CV - 3
        byte[] clientVersion = new byte[3];
        buffer.get(clientVersion); //3
        setClientVersion(clientVersion);

        // MT - 1
        setType(buffer.get()); //1

        // UUID - 16
        byte[] uuid = new byte[16];
        buffer.get(uuid);
        setUUID(uuid); //16

        // PSZ - 2
        byte[] payloadSize = new byte[2];
        buffer.get(payloadSize);
        setPayloadSizeInBytes(payloadSize);

        // CHK - 3
        byte[] payloadChecksum = new byte[3];
        buffer.get(payloadChecksum);
        setPayloadChecksum(payloadChecksum);
    }

    public byte[] getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(byte[] clientVersion) {
        this.clientVersion = clientVersion;
    }

    public byte[] getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(byte[] clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    /**
     * 
     * @return - A 16 byte (128bit) unique identifier for this peer.
     */
    public byte[] getUUID() {
        if (uuid == null) {
            return new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        }
        return uuid;
    }

    /** 
     * The UUID is meant to be a 16 byte array (128bit identifier).
     * 
     * If the given byte array is smaller than that it'll be shifted towards the right, leaving the
     * first (left most bytes) as zeroes.
     *
     * If the given array is bigger than 16 bytes, we'll only copy the 16 right most bytes from it.
     *
     */
    public void setUUID(byte[] uuid) {
        if (uuid == null) {
            this.uuid = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            return;
        }

        if (uuid.length != 16) {
            //we'll pad
            if (uuid.length < 16) {
                //copy it towards the right hand side
                this.uuid = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
                System.arraycopy(uuid, 0, this.uuid, 16 - uuid.length, uuid.length);
            }
            //we'll clip
            else {
                //copy the right-most bytes.
                System.arraycopy(uuid, uuid.length - 16, this.uuid, 0, 16);
            }
        } else {
            this.uuid = new byte[uuid.length];
            System.arraycopy(uuid, 0, this.uuid, 0, uuid.length);
        }
    }

    /**
     * @return the size of the payload as a 2 byte array
     */
    public byte[] getPayloadSizeInBytes() {
        if (payloadSize == null) {
            return new byte[] { 0, 0 };
        }
        return payloadSize;
    }

    /**
     * 
     * @param payloadSize - a 2 byte array representing the size of the payload
     */
    public void setPayloadSizeInBytes(byte[] payloadSize) {
        if (payloadSize.length != 2) {
            throw new IllegalArgumentException("invalid payload size. should be 2 bytes long.");
        }
        this.payloadSize = payloadSize;
    }

    /**
     * Returns the size of the payload as an int
     * 
     * @return
     */
    public int getPayloadSize() {
        return ByteUtils.byteArrayToSmallInt(this.payloadSize, 0);
    }

    /**
     * Set the size of the payload
     * Internally it's stored as a 2 byte array
     * 
     * @param payloadSize
     */
    public void setPayloadSize(int payloadSize) {
        this.payloadSize = ByteUtils.smallIntToByteArray(payloadSize);
    }

    /**
     * Return the checksum of the payload as a 3 byte array
     */
    public byte[] getPayloadChecksum() {
        if (payloadChecksum == null) {
            return new byte[] { 0, 0, 0 };
        }
        return payloadChecksum;
    }

    /**
     * Set the payload checksum bytes
     * 
     * @param payloadChecksum - a 3 byte array representing the checksum integer.
     */
    public void setPayloadChecksum(byte[] payloadChecksum) {
        if (payloadChecksum.length != 3) {
            throw new IllegalArgumentException("invalid payload checksum size. should be 3 bytes long.");
        }
        this.payloadChecksum = payloadChecksum;
    }

    public byte getVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
