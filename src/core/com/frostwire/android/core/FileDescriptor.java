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

package com.frostwire.android.core;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class FileDescriptor implements Cloneable {

    public int id;
    public byte fileType; // As described in Constants.
    public String filePath;
    public long fileSize;
    public String mime; //MIME Type
    public boolean shared;

    public String title;
    // only if audio/video media
    public String artist;
    public String album;
    public String year;
    public int thumbnailId;

    /**
     * Empty constructor.
     */
    public FileDescriptor() {
    }

    public FileDescriptor(int id, String artist, String title, String album, String year, String path, byte fileType, String mime, int thumbnailId, long fileSize, boolean isShared) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.year = year;
        this.filePath = path;
        this.fileType = fileType;
        this.mime = mime;
        this.thumbnailId = thumbnailId;
        this.fileSize = fileSize;
        this.shared = isShared;
    }

    @Override
    public String toString() {
        return "FD(id:" + id + ", ft:" + fileType + ", t:" + title + ", p:" + filePath + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof FileDescriptor)) {
            return false;
        }

        FileDescriptor fd = (FileDescriptor) o;

        return this.id == fd.id && this.fileType == fd.fileType;
    }

    @Override
    public FileDescriptor clone() {
        return new FileDescriptor(id, artist, title, album, year, filePath, fileType, mime, thumbnailId, fileSize, shared);
    }
}
