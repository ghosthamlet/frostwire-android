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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;

import android.util.Log;

import com.frostwire.android.gui.util.SystemUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
class DownloadTorrentTask extends TorrentSearchTask {

    private static final String TAG = "FW.DownloadTorrentTask";

    private final String query;
    private final BittorrentWebSearchResult result;
    private final TorrentSearchTask searchTask;
    private final LocalSearchEngine localSearchEngine;

    private TorrentDownloader torrentDownloader;

    public DownloadTorrentTask(String query, BittorrentWebSearchResult result, TorrentSearchTask searchTask, LocalSearchEngine localSearchEngine) {
        super("DownloadTorrentTask: " + result.getTorrentURI());
        this.query = query;
        this.result = result;
        this.searchTask = searchTask;
        this.localSearchEngine = localSearchEngine;
    }

    @Override
    public void cancel() {
        super.cancel();

        try {
            if (torrentDownloader != null) {
                torrentDownloader.cancel();
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error canceling TorrentDonloader for: " + result.getTorrentURI(), e);
        }
    }

    @Override
    public void run() {

        if (isCancelled()) {
            return;
        }

        try {
            String saveDir = SystemUtils.getDeepScanTorrentsDirectory().getAbsolutePath();

            CountDownLatch finishSignal = new CountDownLatch(1);

            Log.d(TAG, String.format("About to download: %s, details %s", result.getTorrentURI(), result.getTorrentDetailsURL()));
            torrentDownloader = TorrentDownloaderFactory.create(new LocalSearchTorrentDownloaderListener(query, result, searchTask, localSearchEngine, finishSignal), result.getTorrentURI(), result.getTorrentDetailsURL(), saveDir);
            torrentDownloader.start();

            boolean inTime = finishSignal.await(60, TimeUnit.SECONDS);

            if (!inTime) {
                Log.w(TAG, "Download didn't finish in time: " + result.getTorrentURI());
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error in DownloadTorrentTask", e);
        }
    }
}
