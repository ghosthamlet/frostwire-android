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

package com.frostwire.android.bittorrent.websearch.kat;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.frostwire.android.bittorrent.websearch.WebSearchPerformer;
import com.frostwire.android.bittorrent.websearch.WebSearchResult;
import com.frostwire.android.core.HttpFetcher;
import com.frostwire.android.util.JsonUtils;
import com.frostwire.android.util.StringUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class KATWebSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        KATResponse response = searchKAT(keywords);

        if (response != null && response.list != null) {
            for (KATItem bucket : response.list) {

                WebSearchResult sr = new KATWebSearchResult(bucket);

                result.add(sr);
            }
        }

        return result;
    }

    private KATResponse searchKAT(String keywords) {
        String iha = null;
        try {
            iha = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://www.kat.ph/json.php?q=" + iha), HTTP_TIMEOUT);
        } catch (URISyntaxException e) {

        }
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null) {
            return null;
        }

        String json = new String(jsonBytes);

        KATResponse response = JsonUtils.toObject(json, KATResponse.class);

        if (response != null && response.list != null) {
            fixItems(response.list);
        }

        return response;
    }

    /*
     * Include only verified results to keep users safe.
     */
    private void fixItems(List<KATItem> list) {
        if (list != null && list.size() > 0) {
            Iterator<KATItem> iterator = list.iterator();
            while (iterator.hasNext()) {
                KATItem next = iterator.next();

                // Take out non-verified results and elements missing mandatory data
                if (next.verified == 0 || StringUtils.isNullOrEmpty(next.title) || StringUtils.isNullOrEmpty(next.hash) || StringUtils.isNullOrEmpty(next.torrentLink) || StringUtils.isNullOrEmpty(next.link) || next.size <= 0) {
                    iterator.remove();
                    continue;
                }

                // fix https to http
                next.torrentLink = next.torrentLink.replace("https://", "http://");
            }
        }
    }
}