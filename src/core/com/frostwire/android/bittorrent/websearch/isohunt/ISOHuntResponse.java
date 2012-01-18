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

package com.frostwire.android.bittorrent.websearch.isohunt;

/*
 * Queries look like this:
 * http://isohunt.com/js/json.php?ihq=ubuntu&start=21&rows=20&sort=seeds
 * 
 * Where parameters:
 * 
 * ihq Takes url encoded value as requested search query.
 * 
 * start Optional. Starting row number in paging through results set. First page
 * have start=1, not 0. Defaults to 1.
 * 
 * rows Optional. Results to return, starting from parameter "start". Defaults
 * to 100.
 * 
 * sort Optional. Defaults to composite ranking (over all factors such as age,
 * query relevance, seed/leechers counts and votes). Parameter takes only value
 * of "seeds", where ranking is in descending order both seeds and leechers,
 * only.
 * 
 * noSL Optional. Attaching &noSL to URL of the JSON call returns results with
 * torrents that have no seeds or leechers stats, or both having 0. Defaults to
 * excluding them.
 * 
 * 
 * Above same JSON call would search for "ubuntu", returning 2nd page of
 * results, with 20 results per page. Sorts by seeds and leechers, and include
 * torrents with S/L stats only.
 * 
 * rows have upper limit of 100, and start+rows have maximum possible limit of
 * 1000.

 {"title": "isoHunt > All > ubuntu",
  "link": "http://isohunt.com",
  "description": "BitTorrent Search > All > ubuntu",
  "language": "en-us",
  "category": "All",
  "max_results": 1000,
  "ttl": 60,
  "image": {"title": "isoHunt > All > ubuntu",
            "url": "http://isohunt.com/img/buttons/isohunt-02.gif",
            "link": "http://isohunt.com/",
            "width": 157,
            "height": 45},
"lastBuildDate": "Thu, 06 May 2010 04:55:36 GMT",
"pubDate": "Thu, 06 May 2010 04:55:36 GMT",
"total_results": 705, 
"items": {"list":  
[    {"title":"kubuntu-10.04-dvd-i386.iso",
      "link":"http://isohunt.com/torrent_details/184615297/ubuntu?tab=summary",
      "guid":"184615297",
      "enclosure_url":"http://isohunt.com/download/184615297/ubuntu.torrent",
      "length":"3682001224",
      "type":"application/x-bittorrent", 
      "tracker":"torrent.<b>ubuntu</b>.com",
      "tracker_url":"http://torrent.ubuntu.com:6969/announce","kws":"","exempts":"Kubuntu CD cdimage.<b>ubuntu</b>.com","category":"Apps","original_site":"torrent.ubuntu.com:6969",
      "original_link":"http://torrent.ubuntu.com:6969/file?info_hash=%7D%1Ap%ABx%F5%3D%CF%28%5CXW%0D%D6%7C%EDkB%C7%05",
      "size":"3.43 GB","files":"1",
      "Seeds":"224",
      "leechers":"76","downloads":"5","votes":"0","comments":"0","hash":"7d1a70ab78f53dcf285c58570dd67ced6b42c705","pubDate":"Thu, 29 Apr 2010 16:32:44 GMT"},
{"title":"<b>ubuntu</b>-10.04-dvd-amd64.iso","link":"http://isohunt.com/torrent_details/184626289/ubuntu?tab=summary","guid":"184626289","enclosure_url":"http://isohunt.com/download/184626289/ubuntu.torrent","length":"4418143519","type":"application/x-bittorrent", "tracker":"torrent.<b>ubuntu</b>.com","tracker_url":"http://torrent.ubuntu.com:6969/announce","kws":"","exempts":"<b>Ubuntu</b> CD cdimage.<b>ubuntu</b>.com","category":"Apps","original_site":"torrent.ubuntu.com:6969","original_link":"http://torrent.ubuntu.com:6969/file?info_hash=%CF%2A%DB%ACB%AA_%D9%89%05%A8Z%89n%3B%D0%01%1E%EC%A5","size":"4.11 GB","files":"1","Seeds":"193","leechers":"40","downloads":"2","votes":"0","comments":"0","hash":"cf2adbac42aa5fd98905a85a896e3bd0011eeca5","pubDate":"Thu, 29 Apr 2010 18:17:54 GMT"},
*/

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ISOHuntResponse {

    public int total_results;

    public ISOHuntList items;
}
