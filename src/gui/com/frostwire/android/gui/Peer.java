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

package com.frostwire.android.gui;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.core.HttpFetcher;
import com.frostwire.android.core.messages.PingMessage;
import com.frostwire.android.util.JsonUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class Peer implements Cloneable {

    private InetAddress address;
    private int listeningPort;

    /**
     * 16 bytes (128bit - UUID identifier letting us know who is the sender)
     */
    private byte[] uuid;
    private String nickname;
    private int numSharedFiles;
    private String clientVersion;

    private int hashCode = -1;
    private boolean localhost;

    public Peer() {
    }

    public Peer(InetAddress address, int listeningPort, PingMessage ping) {
        this.address = address;
        this.listeningPort = listeningPort;

        setUUID(ping.getUUID());

        this.nickname = ping.getNickname();
        this.numSharedFiles = ping.getNumSharedFiles();
        this.clientVersion = clientVersionToString(ping.getHeader().getClientVersion());
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public byte[] getUUID() {
        return uuid;
    }

    void setUUID(byte[] uuid) {
        this.uuid = new byte[16];
        System.arraycopy(uuid, 0, this.uuid, 0, 16);

        this.hashCode = uuidToHashCode(this.uuid);
        this.localhost = Arrays.equals(this.uuid, ConfigurationManager.instance().getUUID());
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getNumSharedFiles() {
        return numSharedFiles;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public boolean isLocalHost() {
        return localhost;
    }

    public String getFingerUri() {
        return "http://" + address.getHostAddress() + ":" + listeningPort + "/finger";
    }

    public String getBrowseUri(byte fileType) {
        return "http://" + address.getHostAddress() + ":" + listeningPort + "/browse?type=" + fileType;
    }

    public String getDownloadUri(FileDescriptor fd) {
        return "http://" + address.getHostAddress() + ":" + listeningPort + "/download?type=" + fd.fileType + "&id=" + fd.id;
    }

    public Finger finger() {
        if (localhost) {
            return Librarian.instance().finger(localhost);
        } else {
            String uri = getFingerUri();
            byte[] data = new HttpFetcher(uri).fetch();
            String json = new String(data);
            return JsonUtils.toObject(json, Finger.class);
        }
    }

    public List<FileDescriptor> browse(byte fileType) {
        if (localhost) {
            return Librarian.instance().getFiles(fileType, 0, Integer.MAX_VALUE, false);
        } else {
            String uri = getBrowseUri(fileType);
            byte[] data = new HttpFetcher(uri).fetchGzip();
            String json = new String(data);
            return JsonUtils.toObject(json, FileDescriptorList.class).files;
        }
    }

    @Override
    public String toString() {
        return "Peer(" + nickname + "@" + address.getHostAddress() + ", v:" + clientVersion + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Peer)) {
            return false;
        }

        return hashCode() == ((Peer) o).hashCode();
    }

    @Override
    public int hashCode() {
        if (this.hashCode == -1) {
            this.hashCode = uuidToHashCode(uuid);
        }

        return this.hashCode;
    }

    @Override
    public Peer clone() {
        Peer peer = new Peer();

        peer.address = this.address;
        peer.listeningPort = listeningPort;
        peer.setUUID(this.uuid);
        peer.nickname = this.nickname;
        peer.numSharedFiles = this.numSharedFiles;
        peer.clientVersion = this.clientVersion;

        return peer;
    }

    private static String clientVersionToString(byte[] clientVersion) {
        return clientVersion[0] + "." + clientVersion[1] + "." + clientVersion[2];
    }

    /**
     * This method is a weak method to calculate a hash code from a given UUID.
     * @param uuid
     * @return
     */
    private static int uuidToHashCode(byte[] uuid) {

        long msb = (uuid[0] & 0xFFL) << 56;
        msb |= (uuid[1] & 0xFFL) << 48;
        msb |= (uuid[2] & 0xFFL) << 40;
        msb |= (uuid[3] & 0xFFL) << 32;
        msb |= (uuid[4] & 0xFFL) << 24;
        msb |= (uuid[5] & 0xFFL) << 16;
        msb |= (uuid[6] & 0x0FL) << 8;
        msb |= (0x3L << 12); // set the version to 3
        msb |= (uuid[7] & 0xFFL);

        long lsb = (uuid[8] & 0x3FL) << 56;
        lsb |= (0x2L << 62); // set the variant to bits 01
        lsb |= (uuid[9] & 0xFFL) << 48;
        lsb |= (uuid[10] & 0xFFL) << 40;
        lsb |= (uuid[11] & 0xFFL) << 32;
        lsb |= (uuid[12] & 0xFFL) << 24;
        lsb |= (uuid[13] & 0xFFL) << 16;
        lsb |= (uuid[14] & 0xFFL) << 8;
        lsb |= (uuid[15] & 0xFFL);

        int msbHash = (int) (msb ^ (msb >>> 32));
        int lsbHash = (int) (lsb ^ (lsb >>> 32));

        int hash = msbHash ^ lsbHash;

        return hash;
    }

    private static final class FileDescriptorList {
        public List<FileDescriptor> files;
    }
}