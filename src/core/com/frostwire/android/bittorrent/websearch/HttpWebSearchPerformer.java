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

package com.frostwire.android.bittorrent.websearch;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frostwire.android.core.HttpFetcher;

/**
 * Extend this for engines that don't provide JSON APIs.
 *
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class HttpWebSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        try {
            keywords = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }

        HttpFetcher fetcher = new HttpFetcher(getURI(keywords), HTTP_TIMEOUT);
        byte[] htmlBytes = fetcher.fetch();

        if (htmlBytes == null) {
            return result;
        }

        String html = new String(htmlBytes);

        String regex = getRegex();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);

        int max = getMax();

        int i = 0;

        while (matcher.find() && i < max) {
            try {
                WebSearchResult sr = getNextSearchResult(matcher);
                if (sr != null) {
                    result.add(sr);
                    i++;
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        return result;
    }

    /**
     * Returns the URI of the search engine search command.
     */
    protected abstract URI getURI(String keywords);

    /**
     * This method should return an implementation of WebSearchResult using a matcher that is able to find all the torrent field.
     */
    protected abstract WebSearchResult getNextSearchResult(Matcher matcher);

    /**
     * This function must return the regex necessary for a pattern matcher to find the necessary fields of a SearchResult.
     */
    protected abstract String getRegex();

    protected int getMax() {
        return Integer.MAX_VALUE;
    }
}
