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

import com.frostwire.android.bittorrent.websearch.WebSearchResult;
import com.frostwire.android.core.SearchEngine;
import com.frostwire.android.util.FilenameUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class BittorrentWebSearchResult implements BittorrentSearchResult {

    private final SearchEngine searchEngine;
    private final WebSearchResult webResult;

    public BittorrentWebSearchResult(SearchEngine searchEngine, WebSearchResult webResult) {
        this.searchEngine = searchEngine;
        this.webResult = webResult;
    }

    public String getTitle() {
        return FilenameUtils.getName(webResult.getFileName());
    }

    public String getFileName() {
        return webResult.getFileName();
    }

    public int getSeeds() {
        return webResult.getSeeds();
    }

    public long getSize() {
        return webResult.getSize();
    }

    public long getCreationTime() {
        return webResult.getCreationTime();
    }

    public String getHash() {
        return webResult.getHash();
    }

    public int getSearchEngineId() {
        return searchEngine.getId();
    }

    public String getTorrentDetailsURL() {
        return webResult.getTorrentDetailsURL();
    }

    public String getTorrentURI() {
        return webResult.getTorrentURI();
    }

    public String getVendor() {
        return webResult.getVendor();
    }

    @Override
    public String toString() {
        return "(" + "torrent:" + getTorrentURI() + ")";
    }
}
