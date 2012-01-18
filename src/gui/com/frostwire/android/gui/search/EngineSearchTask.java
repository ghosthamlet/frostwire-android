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

package com.frostwire.android.gui.search;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.frostwire.android.bittorrent.websearch.WebSearchResult;
import com.frostwire.android.core.SearchEngine;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
class EngineSearchTask extends TorrentSearchTask {

    private static final String TAG = "FW.PerformTorrentSearchTask";

    private final SearchEngine searchEngine;
    private final SearchResultDisplayer displayer;
    private final String query;

    public EngineSearchTask(SearchEngine searchEngine, SearchResultDisplayer displayer, String query) {
        super("EngineSearchTask: " + searchEngine.getName() + " -: " + query);
        this.searchEngine = searchEngine;
        this.displayer = displayer;
        this.query = query;
    }

    public void run() {
        try {
            List<WebSearchResult> webResults = searchEngine.getPerformer().search(query);
            List<SearchResult> results = normalizeWebResults(searchEngine, webResults);
            displayer.addResults(results);
        } catch (Throwable e) {
            Log.e(TAG, "Error getting data from " + searchEngine, e);
        }
    }

    private static List<SearchResult> normalizeWebResults(SearchEngine engine, List<WebSearchResult> webResults) {
        List<SearchResult> result = new ArrayList<SearchResult>(webResults.size());
        for (WebSearchResult webResult : webResults) {
            SearchResult sr = new BittorrentWebSearchResult(engine, webResult);
            result.add(sr);
        }
        return result;
    }
}
