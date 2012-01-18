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

import android.content.Context;
import android.util.Log;

import com.frostwire.android.core.SearchEngine;
import com.frostwire.android.gui.services.Engine;

/**
 * @author aldenml
 * @author gubatron
 *
 */
public final class BittorrentSearchEngine {

    private static final String TAG = "FW.BittorrentSearchEngine";

    private final Context context;
    private final SearchResultDisplayer displayer;

    private final List<TorrentSearchTask> searchTasks;

    public BittorrentSearchEngine(Context context, SearchResultDisplayer displayer) {
        this.context = context;
        this.displayer = displayer;

        searchTasks = new ArrayList<TorrentSearchTask>();
    }

    public void performSearch(String query) {
        cancelTasks();
        displayer.clear();
        performTorrentSearch(query);
    }

    public void performTorrentSearch(String query) {
        execute(new LocalSearchTask(context, displayer, query));

        for (SearchEngine searchEngine : SearchEngine.getSearchEngines()) {
            if (searchEngine.isEnabled()) {
                execute(new EngineSearchTask(searchEngine, displayer, query));
            }
        }

        execute(new DeepSearchTask(context, displayer, query));
    }

    public void cancelSearch() {
        cancelTasks();
    }

    public static int getLocalIndexCount(Context context) {
        return LocalSearchEngine.getIndexCount(context);
    }

    public static int clearLocalIndex(Context context) {
        return LocalSearchEngine.clearIndex(context);
    }

    private void execute(TorrentSearchTask task) {
        searchTasks.add(task);
        Engine.instance().getThreadPool().execute(task);
    }

    private void cancelTasks() {
        for (TorrentSearchTask task : searchTasks) {
            try {
                task.cancel();
            } catch (Throwable e) {
                Log.e(TAG, "Failed to cancel search task", e);
            }
        }

        searchTasks.clear();
    }
}