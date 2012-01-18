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

package com.frostwire.android.bittorrent.websearch.clearbits;

import java.util.List;

/* 
  ClearBits JSON Responses look like this:
   [{"torrent": 
    {"leechers": 0, 
    "created_at": "2010-07-15T16:02:42Z", 
    "title": "Bear and Lampshade - Siddhartha", 
    "seeds": 3, 
    "hashstr": "faabc8daf2e33e9ed6058b8acc1819c9bf35177a", 
    "mb_size": 65, 
    "license_url": "http://creativecommons.org/licenses/by-nc-nd/3.0/", 
    "torrent_url": "http://www.clearbits.net/get/1235-bear-and-lampshade---siddhartha.torrent", 
    "location": "http://www.clearbits.net/torrents/1235-bear-and-lampshade---siddhartha"}
    }, 
    
 {"torrent": 
    {"leechers": 0, 
    "created_at": "2010-06-23T06:34:19Z", 
    "title": "Yokandesh - Viva Como Queira", 
    "seeds": 5, 
    "hashstr": "d7fc71b8554befcebd125176826fa1f41bead46a", 
    "mb_size": 136, 
    "license_url": "http://creativecommons.org/licenses/by-nc-nd/3.0/", 
    "torrent_url": "http://www.clearbits.net/get/1196-yokandesh---viva-como-queira.torrent", 
    "location": "http://www.clearbits.net/torrents/1196-yokandesh---viva-como-queira"}}, ...
 */
/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ClearBitsResponse {

    public List<ClearBitsItem> results;

    /**
     * 
     */
    public void fixItems() {
        if (results != null && results.size() > 0) {
            for (ClearBitsItem item : results) {
                item.fixItem();
            }
        }
    }
}
