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

package com.frostwire.android.bittorrent.websearch.mininova;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.android.bittorrent.websearch.WebSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class MininovaVuzeWebSearchResult implements WebSearchResult {

    private MininovaVuzeItem item;

    public MininovaVuzeWebSearchResult(MininovaVuzeItem item) {
        this.item = item;
    }

    public long getCreationTime() {
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(item.date).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    public String getFileName() {
        String titleNoTags = item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    public String getHash() {
        return item.hash;
    }

    public String getTorrentURI() {
        return item.download;
    }

    public long getSize() {
        return Long.valueOf(item.size);
    }

    public String getVendor() {
        return "Mininova";
    }

    public int getSeeds() {
        return item.seeds + item.superseeds;
    }

    public String getTorrentDetailsURL() {
        return item.cdp;
    }
}
