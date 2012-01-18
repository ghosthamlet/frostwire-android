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

package com.frostwire.android.bittorrent.websearch.btjunkie;

/*
{ "results": [
 {
  "title": "...",
   "date": "Sat, 10 Oct 2009 00:00:00 +0000",
   "peers": 131,
   "seeds": 630,
   "category": "Audio",
   "cdp": "...",
   "comments": 83,
   "size": 99614720,
   "votes": 999,
   "download": "...",
   "hash": "..."
 },
*/
/**
 * @author gubatron
 * @author aldenml
 *
 */
public class BTJunkieItem {

    public String title;

    public String date;

    public int peers;

    public int seeds;

    public String category;

    public String cdp;

    public int comments;

    public long size;

    public int votes;

    public String download;

    public String hash;
}
