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

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ClearBitsItem {

    private final static String CLEARBITS_TORRENT_GET_URL_PREFIX = "http://www.clearbits.net/get/";
    private final static String CLEARBITS_TORRENT_PAGE_URL_PREFIX = "http://www.clearbits.net/torrents/";

    /*
     {"leechers": 0, 
    "created_at": "2010-07-15T16:02:42Z", 
    "title": "Bear and Lampshade - Siddhartha", 
    "seeds": 3, 
    "hashstr": "faabc8daf2e33e9ed6058b8acc1819c9bf35177a", 
    "mb_size": 65, 
    "license_url": "http://creativecommons.org/licenses/by-nc-nd/3.0/", 
    "torrent_url": "http://www.clearbits.net/get/1235-bear-and-lampshade---siddhartha.torrent", 
    "location": "http://www.clearbits.net/torrents/1235-bear-and-lampshade---siddhartha"}
    }
     */
    public int id;
    public String filename;

    public String strippedFilename;

    public int leechers;
    public String created_at;
    public String title;
    public int seeds;
    public String hashstr;
    public int mb_size;
    public String license_url;

    /* no longer available. calculated in fixItem() */
    public String torrent_url;

    /* no longer available. calculated in fixItem() */
    public String location;

    /*
     Fixes any issues related to missing data as we used to know it.
     
     As of March 2011 results look like this 
     
     {"subtitle_url":null,
    "leechers":0,
    "format":"mp3, flac, wav",
    "feature_weight":0,
    "created_at":"2009-06-21T06:07:51Z",
    "title":"Bert Jerred - Volume 7",
    "seeds":1,
    "updated_at":"2011-03-07T07:48:56Z",
    "private":0,
    "hashstr":"5796a8039f810510d90a181c49c1df33462e16a5",
    "mb_size":109,
    "upload_status":"Complete",
    "id":593, <<< AHA!
    "download_hist":11695,
    "creator_id":119,
    "language_id":3,
    "featured_at":null,
    "category_id":6,"upload_id":552,
    "filename":"l/u/m/e/bert jerred - volume 7.torrent", <<< AHA!
    "version_title":null,
    "upload_host":"10.12.36.88",
    "license_url":"http://creativecommons.org/licenses/by/3.0/",
    "download_trkr":5,
    "description":"Another collection (2002-2009) from Bert Jerred",
    "web_statement_url":null,
    "announced":true,"version_of":null,
    "outside_url":"http://www.jamendo.com/en/artist/bert_jerred","active":true},

    The torrent URL is basically: http://www.clearbits.net/get/<id>-<transformed filename>
    
    Where transformed filename, is the name of the torrent, without the path prefixes, and with all spaces replaced with "-"
    
    In the case of this result the torrent url is as follows:
    http://www.clearbits.net/get/593-bert-jerred---volume-7.torrent
    
    Something similar is done to the old "location" field. 
    */
    public void fixItem() {
        if (torrent_url != null && torrent_url.length() > 0) {
            return;
        }

        StringBuilder urlBuilder = new StringBuilder();

        strippedFilename = filename.substring(filename.lastIndexOf("/") + 1).replace(" ", "-");

        //fix torrent url
        urlBuilder.append(CLEARBITS_TORRENT_GET_URL_PREFIX);
        urlBuilder.append(id);
        urlBuilder.append("-");
        urlBuilder.append(strippedFilename);
        torrent_url = urlBuilder.toString();

        //fix clearbit webpage url
        String filenameNoExtension = strippedFilename.substring(0, strippedFilename.indexOf(".torrent"));

        urlBuilder = new StringBuilder();
        urlBuilder.append(CLEARBITS_TORRENT_PAGE_URL_PREFIX);
        urlBuilder.append(id);
        urlBuilder.append("-");
        urlBuilder.append(filenameNoExtension);
        location = urlBuilder.toString();

        //title = "(Clearbits.net) " + title;
    }
}
