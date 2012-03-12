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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.util.TorrentUtils;

import android.util.Log;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
class LocalSearchTorrentDownloaderListener implements TorrentDownloaderCallBackInterface {

    private static final String TAG = "FW.LocalSearchTorrentDownloaderListener";

    private final AtomicBoolean finished = new AtomicBoolean(false);

    private final Set<String> tokens;
    private final BittorrentWebSearchResult result;
    private final TorrentSearchTask searchTask;
    private final LocalSearchEngine localSearchEngine;
    private final CountDownLatch finishSignal;

    public LocalSearchTorrentDownloaderListener(String query, BittorrentWebSearchResult result, TorrentSearchTask searchTask, LocalSearchEngine localSearchEngine, CountDownLatch finishSignal) {
        this.tokens = new HashSet<String>(Arrays.asList(query.toLowerCase().split(" ")));
        this.result = result;
        this.searchTask = searchTask;
        this.localSearchEngine = localSearchEngine;
        this.finishSignal = finishSignal;
    }

    @Override
    public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {
        // index the torrent (insert it's structure in the local DB)
        if (state == TorrentDownloader.STATE_FINISHED && finished.compareAndSet(false, true)) {
            try {
                File torrentFile = inf.getFile();
                TOTorrent theTorrent = TorrentUtils.readFromFile(torrentFile, false);

                if (!searchTask.isCancelled() && tokens.size() > 0) {
                    // search right away on this torrent.
                    matchResults(theTorrent);
                }

                localSearchEngine.indexTorrent(result, theTorrent);

                torrentFile.delete();
            } catch (Throwable e) {
                Log.e(TAG, "Error indexing a torrent: " + result.getTorrentURI(), e);
            }

            finishSignal.countDown();
        }

        switch (state) {
        case TorrentDownloader.STATE_FINISHED:
        case TorrentDownloader.STATE_ERROR:
        case TorrentDownloader.STATE_DUPLICATE:
        case TorrentDownloader.STATE_CANCELLED:
            finishSignal.countDown();
            break;
        }
    }

    private void matchResults(TOTorrent theTorrent) {
        TOTorrentFile[] fs = theTorrent.getFiles();
        for (int i = 0; i < fs.length && !searchTask.isCancelled(); i++) {
            try {
                String keywords = LocalSearchEngine.sanitize(result.getFileName() + " " + fs[i].getRelativePath()).toLowerCase();

                boolean foundMatch = true;

                for (String token : tokens) {
                    if (!keywords.contains(token)) {
                        foundMatch = false;
                        break;
                    }
                }

                if (foundMatch) {
                    localSearchEngine.addResult(new BittorrentDeepSearchResult(result, fs[i]));
                }
            } catch (Throwable e) {
                Log.e(TAG, "Error testing match for inner file of torrent", e);
            }
        }
    }
}
