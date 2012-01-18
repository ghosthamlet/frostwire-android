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

package com.frostwire.android.bittorrent.websearch.tpb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

import com.frostwire.android.bittorrent.websearch.HttpWebSearchPerformer;
import com.frostwire.android.bittorrent.websearch.WebSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TPBWebSearchPerformer extends HttpWebSearchPerformer {

    @Override
    public URI getURI(String encodedKeywords) {
        try {
            return new URI("http://thepiratebay.org/search/" + encodedKeywords + "/0/7/0");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public WebSearchResult getNextSearchResult(final Matcher matcher) {
        return new TPBWebSearchResult(matcher);
    }

    @Override
    public String getRegex() {
        //smoke this joint
        return "(?is)<td class=\"vertTh\">.*?<a href=\"[^\"]*?\" title=\"More from this category\">(.*?)</a>.*?</td>.*?<a href=\"([^\"]*?)\" class=\"detLink\" title=\"Details for ([^\"]*?)\">.*?</a>.*?<a href=\"([^\"]*?)\" title=\"Download this torrent\">.*?</a>.*?<a href=\"magnet:\\?xt=urn:btih:([\\w]*).*?[^\"]*?\" title=\"Download this torrent using magnet\">.*?</a>.*?<font class=\"detDesc\">Uploaded ([^,]*?), Size (.*?), ULed.*?<td align=\"right\">(.*?)</td>\\s*<td align=\"right\">(.*?)</td>";
    }
}
