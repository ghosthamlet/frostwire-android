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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.android.bittorrent.websearch.WebSearchPerformer;
import com.frostwire.android.bittorrent.websearch.WebSearchResult;
import com.frostwire.android.core.HttpFetcher;
import com.frostwire.android.util.JsonUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class BTJunkieWebSearchPerformer implements WebSearchPerformer {

    @Override
    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        BTJunkieResponse response = searchBTJunkie(keywords);

        if (response != null && response.results != null)
            for (BTJunkieItem item : response.results) {

                WebSearchResult sr = new BTJunkieResponseWebSearchResult(item);

                result.add(sr);
            }

        return result;
    }

    private BTJunkieResponse searchBTJunkie(String keywords) {
        String iha = null;
        try {
            iha = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://btjunkie.org/json.php?q=" + iha));
        } catch (URISyntaxException e) {
        }
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null) {
            return null;
        }

        String json = new String(jsonBytes);
        json = json.replace("&#039", "'");

        BTJunkieResponse response = null;

        try {
            response = JsonUtils.toObject(json, BTJunkieResponse.class);
        } catch (Exception e) {
        }

        return response;
    }
}
