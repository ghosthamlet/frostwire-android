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

package com.frostwire.android.bittorrent.websearch.extratorrent;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.android.bittorrent.websearch.WebSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ExtratorrentResponseWebSearchResult implements WebSearchResult {

    private final ExtratorrentItem item;

    public ExtratorrentResponseWebSearchResult(ExtratorrentItem item) {
        this.item = item;
    }

    public String getFileName() {
        String titleNoTags = item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    public long getSize() {
        return item.size;
    }

    public long getCreationTime() {
        //Wed, 09 Jun 2010 18:08:27 +0100
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(item.pubDate).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    public String getVendor() {
        return "Extratorrent";
    }

    public String getHash() {
        return item.hash;
    }

    public String getTorrentURI() {
        return item.torrentLink;
    }

    public int getSeeds() {
        return item.seeds;
    }

    public String getTorrentDetailsURL() {
        return item.link;
    }
}
