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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;
import org.gudy.azureus2.core3.util.TorrentUtils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.SearchEngine;
import com.frostwire.android.core.providers.UniversalStore.Torrents;
import com.frostwire.android.core.providers.UniversalStore.Torrents.TorrentFilesColumns;
import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.android.util.JsonUtils;
import com.frostwire.android.util.StringUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
final class LocalSearchEngine {

    private static final String TAG = "FW.LocalSearchEngine";

    private final Context context;
    private final TorrentSearchTask task;
    private final SearchResultDisplayer displayer;
    private final String query;

    private final int count;
    private final int rounds;
    private final int interval;
    private final int seeds;
    private final int maxTorrentFiles;
    private final int ftsLimit;

    private final HashSet<String> knownInfoHashes;

    private int downloaded;

    public LocalSearchEngine(Context context, TorrentSearchTask task, SearchResultDisplayer displayer, String query) {
        this.context = context;
        this.task = task;
        this.displayer = displayer;
        this.query = sanitize(query);

        ConfigurationManager configuration = ConfigurationManager.instance();

        count = configuration.getInt(Constants.PREF_KEY_SEARCH_COUNT_DOWNLOAD_FOR_TORRENT_DEEP_SCAN);
        rounds = configuration.getInt(Constants.PREF_KEY_SEARCH_COUNT_ROUNDS_FOR_TORRENT_DEEP_SCAN);
        interval = configuration.getInt(Constants.PREF_KEY_SEARCH_INTERVAL_MS_FOR_TORRENT_DEEP_SCAN);
        seeds = configuration.getInt(Constants.PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_DEEP_SCAN);
        maxTorrentFiles = configuration.getInt(Constants.PREF_KEY_SEARCH_MAX_TORRENT_FILES_TO_INDEX);
        ftsLimit = configuration.getInt(Constants.PREF_KEY_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT);

        knownInfoHashes = new HashSet<String>();
    }

    public void deepSearch() {
        downloaded = 0;
        SystemClock.sleep(interval);

        for (int i = 0; i < rounds && !task.isCancelled(); i++) {

            scanDisplayer(i);

            SystemClock.sleep(interval);
        }
    }

    public static int getIndexCount(Context context) {
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(Torrents.Media.CONTENT_URI, new String[] { TorrentFilesColumns._ID }, null, null, null);
            return c.getCount();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static int clearIndex(Context context) {
        ContentResolver cr = context.getContentResolver();
        cr.delete(Torrents.Media.CONTENT_URI_SEARCH, null, null);
        return cr.delete(Torrents.Media.CONTENT_URI, null, null);
    }

    public List<SearchResult> search(String query) {
        List<Integer> ids = new ArrayList<Integer>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = null;
        try {
            c = cr.query(Torrents.Media.CONTENT_URI_SEARCH, new String[] { "rowid" }, null, new String[] { buildFtsQuery(query) }, " torrent_seeds DESC LIMIT " + ftsLimit);
            while (c.moveToNext()) {
                ids.add(c.getInt(0));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        try {
            long start = System.currentTimeMillis();
            c = cr.query(Torrents.Media.CONTENT_URI, new String[] { TorrentFilesColumns.JSON }, "_id IN " + StringUtils.buildSet(ids), null, "torrent_seeds DESC LIMIT " + ftsLimit);
            long delta = System.currentTimeMillis() - start;
            Log.i(TAG, "Found " + c.getCount() + " local results in " + delta + "ms. ");
            //no query should ever take this long.
            if (delta > 3000) {
                Log.w(TAG, "Warning: Results took too long, there's something wrong with the database, you might want to delete some data.");
            }

            List<SearchResult> results = new ArrayList<SearchResult>();
            Map<Integer, SearchEngine> searchEngines = SearchEngine.getSearchEngineMap();

            while (c.moveToNext()) {
                try {
                    String json = c.getString(0);

                    TorrentFileDB tfdb = JsonUtils.toObject(json, TorrentFileDB.class);

                    if (!searchEngines.get(tfdb.torrent.searchEngineID).isEnabled()) {
                        continue;
                    }

                    results.add(new BittorrentLocalSearchResult(tfdb));
                    knownInfoHashes.add(tfdb.torrent.hash);
                } catch (Exception e) {
                    Log.e(TAG, "Error reading local search result", e);
                }
            }

            Log.i(TAG, "Ended up with " + results.size() + " results");

            return results;

        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void scanDisplayer(int round) {
        List<SearchResult> results = displayer.getResults();

        for (int i = 0; i < results.size() && downloaded < count && !task.isCancelled(); i++) {
            SearchResult sr = results.get(i);
            if (sr instanceof BittorrentWebSearchResult) {
                BittorrentWebSearchResult bsr = (BittorrentWebSearchResult) sr;

                if (bsr.getHash() != null && (bsr.getSeeds() > seeds || isRare(round, results.size())) && !torrentIndexed(bsr)) {
                    if (!knownInfoHashes.contains(bsr.getHash())) {
                        knownInfoHashes.add(bsr.getHash());
                        downloaded++;
                        downloadAndScan(bsr);
                    }
                }
            }
        }
    }

    public boolean isRare(int round, int searchResultsCount) {
        return round == rounds - 1 && searchResultsCount < 50;
    }

    private TorrentDownloader downloadAndScan(BittorrentWebSearchResult result) {
        TorrentDownloader td = null;

        String saveDir = SystemUtils.getDeepScanTorrentsDirectory().getAbsolutePath();
        try {
            td = TorrentDownloaderFactory.create(new LocalSearchTorrentDownloaderListener(query, result), result.getTorrentURI(), null, saveDir);
            td.start();
        } catch (Throwable e) {
            Log.e(TAG, "Error creating a torrent downloader for result: " + result);
        }

        return td;
    }

    private boolean torrentIndexed(BittorrentWebSearchResult result) {
        ContentResolver cr = context.getContentResolver();
        Cursor c = null;
        try {
            c = cr.query(Torrents.Media.CONTENT_URI, new String[] { TorrentFilesColumns._ID }, "TORRENT_INFO_HASH LIKE ?", new String[] { result.getHash() }, null);
            return c.getCount() > 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void indexTorrent(BittorrentWebSearchResult result, TOTorrent torrent) {
        TorrentDB tdb = new TorrentDB();
        tdb.creationTime = result.getCreationTime();
        tdb.fileName = result.getFileName();
        tdb.hash = result.getHash();
        tdb.searchEngineID = result.getSearchEngineId();
        tdb.seeds = result.getSeeds();
        tdb.size = result.getSize();
        tdb.torrentDetailsURL = result.getTorrentDetailsURL();
        tdb.torrentURI = result.getTorrentURI();
        tdb.vendor = result.getVendor();

        TOTorrentFile[] files = torrent.getFiles();

        long now = System.currentTimeMillis();

        for (int i = 0; i < files.length && i < maxTorrentFiles; i++) {
            TOTorrentFile f = files[i];
            TorrentFileDB tfdb = new TorrentFileDB();
            tfdb.relativePath = f.getRelativePath();
            tfdb.size = f.getLength();
            tfdb.torrent = tdb;

            String keywords = sanitize(tdb.fileName + " " + tfdb.relativePath).toLowerCase();
            String json = JsonUtils.toJson(tfdb);

            insert(now, tdb.hash, tdb.fileName, tdb.seeds, tfdb.relativePath, keywords, json);
            Thread.yield(); // try to play nice with others
        }
    }

    private void insert(long timestamp, String torrentInfoHash, String torrentFileName, int torrentSeeds, String relativePath, String keywords, String json) {
        ContentResolver cr = context.getContentResolver();

        ContentValues cv = new ContentValues();

        cv.put(TorrentFilesColumns.TIMESTAMP, timestamp);
        cv.put(TorrentFilesColumns.TORRENT_INFO_HASH, torrentInfoHash);
        cv.put(TorrentFilesColumns.TORRENT_FILE_NAME, torrentFileName);
        cv.put(TorrentFilesColumns.TORRENT_SEEDS, torrentSeeds);
        cv.put(TorrentFilesColumns.RELATIVE_PATH, relativePath);
        cv.put(TorrentFilesColumns.KEYWORDS, keywords);
        cv.put(TorrentFilesColumns.JSON, json);

        cr.insert(Torrents.Media.CONTENT_URI, cv);
    }

    private String buildFtsQuery(String query) {
        query = sanitize(query);

        Set<String> tokens = new HashSet<String>(Arrays.asList(query.toLowerCase().split(" ")));

        String fts = "";

        for (String token : tokens) {
            fts += token.toLowerCase() + " ";
        }

        return fts.trim();
    }

    private final static String sanitize(String str) {
        str = Html.fromHtml(str).toString();
        str = str.replaceAll("\\.torrent|www\\.|\\.com|[\\\\\\/%_;\\-\\.\\(\\)\\[\\]\\n\\r–]", " ");
        return StringUtils.removeDoubleSpaces(str);
    }

    private class LocalSearchTorrentDownloaderListener implements TorrentDownloaderCallBackInterface {

        private final AtomicBoolean finished = new AtomicBoolean(false);

        private final Set<String> tokens;
        private final BittorrentWebSearchResult result;

        public LocalSearchTorrentDownloaderListener(String query, BittorrentWebSearchResult result) {
            this.tokens = new HashSet<String>(Arrays.asList(query.toLowerCase().split(" ")));
            this.result = result;
        }

        @Override
        public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {
            // index the torrent (insert it's structure in the local DB)
            if (state == TorrentDownloader.STATE_FINISHED && finished.compareAndSet(false, true)) {
                try {
                    File torrentFile = inf.getFile();
                    TOTorrent theTorrent = TorrentUtils.readFromFile(torrentFile, false);

                    if (!task.isCancelled() && tokens.size() > 0) {
                        // search right away on this torrent.
                        matchResults(theTorrent);
                    }

                    indexTorrent(result, theTorrent);

                    torrentFile.delete();
                } catch (Throwable e) {
                    Log.e(TAG, "Error indexing a torrent: " + result.getTorrentURI(), e);
                }
            }

            switch (state) {
            case TorrentDownloader.STATE_FINISHED:
            case TorrentDownloader.STATE_ERROR:
            case TorrentDownloader.STATE_DUPLICATE:
            case TorrentDownloader.STATE_CANCELLED:
                break;
            }
        }

        private void matchResults(TOTorrent theTorrent) {
            TOTorrentFile[] fs = theTorrent.getFiles();
            for (int i = 0; i < fs.length && !task.isCancelled(); i++) {
                try {
                    String keywords = sanitize(result.getFileName() + " " + fs[i].getRelativePath()).toLowerCase();

                    boolean foundMatch = true;

                    for (String token : tokens) {
                        if (!keywords.contains(token)) {
                            foundMatch = false;
                            break;
                        }
                    }

                    if (foundMatch) {
                        displayer.addResult(new BittorrentDeepSearchResult(result, fs[i]));
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Error testing match for inner file of torrent", e);
                }
            }
        }
    }
}
