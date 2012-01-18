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

package com.frostwire.android.gui.search;

import com.frostwire.android.util.FilenameUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
class BittorrentLocalSearchResult implements BittorrentSearchResult {

    private final TorrentFileDB tfdb;

    public BittorrentLocalSearchResult(TorrentFileDB tfdb) {
        this.tfdb = tfdb;
    }

    public String getTitle() {
        return FilenameUtils.getName(tfdb.relativePath);
    }

    public String getFileName() {
        return tfdb.relativePath;
    }

    public int getSeeds() {
        return tfdb.torrent.seeds;
    }

    public long getSize() {
        return tfdb.size;
    }

    public long getCreationTime() {
        return tfdb.torrent.creationTime;
    }

    @Override
    public String getHash() {
        return tfdb.torrent.hash;
    }

    @Override
    public int getSearchEngineId() {
        return tfdb.torrent.searchEngineID;
    }

    @Override
    public String getTorrentDetailsURL() {
        return tfdb.torrent.torrentDetailsURL;
    }

    @Override
    public String getTorrentURI() {
        return tfdb.torrent.torrentURI;
    }

    @Override
    public String getVendor() {
        return tfdb.torrent.vendor;
    }
}
