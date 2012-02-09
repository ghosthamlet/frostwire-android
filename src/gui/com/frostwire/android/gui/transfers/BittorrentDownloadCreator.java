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

package com.frostwire.android.gui.transfers;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.TorrentUtils;

import android.util.Log;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.gui.search.BittorrentIntentFileResult;
import com.frostwire.android.gui.search.BittorrentIntentHttpResult;
import com.frostwire.android.gui.search.BittorrentSearchResult;
import com.frostwire.android.gui.search.BittorrentWebSearchResult;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.android.util.ByteUtils;
import com.frostwire.android.util.StringUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
final class BittorrentDownloadCreator {

    private static final String TAG = "FW.BittorrentDownloadCreator";

    private BittorrentDownloadCreator() {
    }

    public static BittorrentDownload create(TransferManager manager, BittorrentSearchResult sr, String torrentFile) throws TOTorrentException {
        byte[] hash = null;
        try {
            if (sr.getHash() != null) {
                hash = ByteUtils.decodeHex(sr.getHash());
            }
        } catch (Throwable e) {
            // ignore
        }
        if (sr instanceof BittorrentWebSearchResult || 
            sr instanceof BittorrentPromotionSearchResult ||
            sr instanceof BittorrentIntentHttpResult) {
            return create(manager, torrentFile, hash, null);
        } else {
            return create(manager, torrentFile, hash, sr.getFileName());
        }
    }

    public static BittorrentDownload create(TransferManager manager, BittorrentSearchResult sr) throws TOTorrentException {
        if (!AzureusManager.isCreated()) {
            return new InvalidBittorrentDownload(R.string.azureus_manager_not_created);
        }
        
        GlobalManager gm = AzureusManager.instance().getGlobalManager();

        if (sr instanceof BittorrentIntentFileResult) {
            BittorrentIntentFileResult bifr = (BittorrentIntentFileResult) sr;
            TOTorrent torrent = TorrentUtils.readFromFile(new File(sr.getFileName()), false);
            return create(manager, bifr.getFileName(), torrent.getHash(), null);
        }
        else if (StringUtils.isNullOrEmpty(sr.getHash())) {
            return new TorrentFetcherDownload(manager, sr);
        } else {
            Log.d(TAG, "About to create download for hash: " + sr.getHash());
            DownloadManager dm = gm.getDownloadManager(new HashWrapper(ByteUtils.decodeHex(sr.getHash())));
            if (dm == null) {// new download, I need to download the torrent
                Log.d(TAG, "Creating new TorrentFetcherDownload for hash: " + sr.getHash());
                return new TorrentFetcherDownload(manager, sr);
            } else {
                if (sr instanceof BittorrentWebSearchResult) {
                    return create(manager, dm.getTorrentFileName(), dm.getTorrent().getHash(), null);
                } else {
                    return create(manager, dm.getTorrentFileName(), dm.getTorrent().getHash(), sr.getFileName());
                }
            }
        }
    }

    static BittorrentDownload create(TransferManager manager, DownloadManager dm) {
        setup(dm, false);

        return new AzureusBittorrentDownload(manager, dm);
    }

    /**
     * 
     * @param manager
     * @param torrentFile
     * @param hash
     * @param relativePartialPath - In case you want to download some files.
     * @return
     * @throws TOTorrentException
     */
    private static BittorrentDownload create(TransferManager manager, String torrentFile, byte[] hash, String relativePartialPath) throws TOTorrentException {
        GlobalManager gm = AzureusManager.instance().getGlobalManager();
        TOTorrent torrent = null;
        DownloadManager dm = null;

        if (hash == null) {
            torrent = TorrentUtils.readFromFile(new File(torrentFile), false);
            hash = torrent.getHash();
        }

        if (hash != null) {
            dm = gm.getDownloadManager(new HashWrapper(hash));
        }

        if (dm == null) {
            boolean[] fileSelection = null;
            if (relativePartialPath != null) {
                if (torrent == null) {
                    torrent = TorrentUtils.readFromFile(new File(torrentFile), false);
                }
                fileSelection = buildFileSelection(torrent, relativePartialPath);
            }
            dm = createDownloadManager(manager, torrentFile, fileSelection);
        } else { //the download manager was there...

            boolean[] fileSelection = null;

            if (relativePartialPath != null) { //I want to download partial files.

                fileSelection = buildFileSelection(dm, relativePartialPath);
                boolean[] prevSelection = getFileSelection(dm);

                //he was already downloading the whole torrent, you'll get the file eventually when it finishes.
                if (isDownloadingAll(prevSelection)) {
                    return new InvalidBittorrentDownload(R.string.file_is_already_downloading);
                }

                //let the new fileSelection know about the older files that were selected for download
                //(union)
                for (int i = 0; i < fileSelection.length; i++) {
                    if (prevSelection[i]) {
                        fileSelection[i] = true;
                    }
                }

            } else { // I want to download the whole thing
                boolean[] prevSelection = getFileSelection(dm);
                if (isDownloadingAll(prevSelection)) {
                    return new InvalidBittorrentDownload(R.string.file_is_already_downloading);
                }
            }

            findDownload(manager, dm).cancel();

            dm = createDownloadManager(manager, dm.getTorrentFileName(), fileSelection);
        }

        setup(dm, true);

        return new AzureusBittorrentDownload(manager, dm);
    }

    private static boolean isDownloadingAll(boolean[] fileSelection) {
        for (int i = 0; i < fileSelection.length; i++) {
            if (!fileSelection[i]) {
                return false;
            }
        }
        return true;
    }

    private static void setupPartialSelection(DownloadManager dm, boolean[] fileSelection) {
        DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            boolean[] toSkip = new boolean[fileInfos.length];
            boolean[] toCompact = new boolean[fileInfos.length];

            int compNum = 0;

            for (int iIndex = 0; iIndex < fileInfos.length; iIndex++) {
                DiskManagerFileInfo fileInfo = fileInfos[iIndex];
                File fDest = fileInfo.getFile(true);

                if (!fileSelection[iIndex]) {
                    toSkip[iIndex] = true;
                    if (!fDest.exists()) {
                        toCompact[iIndex] = true;
                        compNum++;
                    }
                }
            }

            if (compNum > 0) {
                dm.getDiskManagerFileInfoSet().setStorageTypes(toCompact, DiskManagerFileInfo.ST_COMPACT);
            }

            dm.getDiskManagerFileInfoSet().setSkipped(toSkip, true);

        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }

    private static BittorrentDownload findDownload(TransferManager manager, DownloadManager dm) {
        for (BittorrentDownload download : manager.getBittorrentDownloads()) {
            BittorrentDownload btDownload = download;
            if (download instanceof TorrentFetcherDownload) {
                btDownload = ((TorrentFetcherDownload) download).getDelegate();
            }
            if (btDownload != null) {
                if (btDownload instanceof AzureusBittorrentDownload) {
                    if (((AzureusBittorrentDownload) btDownload).getDownloadManager().equals(dm)) {
                        return download;
                    }
                }
            }
        }
        return null;
    }

    private static void setup(DownloadManager dm, final boolean notifyFinished) {
        dm.addListener(new DownloadManagerAdapter() {

            private AtomicBoolean finished = new AtomicBoolean(false);

            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                }

                if (TorrentUtil.isComplete(manager) && finished.compareAndSet(false, true)) {
                    if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS)) {
                        TorrentUtil.stop(manager);
                    }

                    if (notifyFinished) {
                        TransferManager.instance().incrementDownloadsToReview();
                        Engine.instance().notifyDownloadFinished(manager.getDisplayName(), manager.getSaveLocation().getAbsoluteFile());
                        Librarian.instance().scan(manager.getSaveLocation().getAbsoluteFile());
                    }
                }
            }
        });

        if (dm.getState() != DownloadManager.STATE_STOPPED) {
            dm.initialize();
        }
    }

    private static boolean[] getFileSelection(DownloadManager dm) {
        boolean[] fileSelections = new boolean[dm.getDiskManagerFileInfoSet().getFiles().length];
        for (int i = 0; i < dm.getDiskManagerFileInfoSet().getFiles().length; i++) {
            fileSelections[i] = !dm.getDiskManagerFileInfoSet().getFiles()[i].isSkipped();
        }

        return fileSelections;
    }

    private static boolean[] buildFileSelection(TOTorrent torrent, String relativePath) throws TOTorrentException {
        boolean[] filesSelection = new boolean[torrent.getFiles().length];
        for (int i = 0; i < filesSelection.length; i++) {
            filesSelection[i] = torrent.getFiles()[i].getRelativePath().equals(relativePath);
        }
        return filesSelection;
    }

    private static boolean[] buildFileSelection(DownloadManager dm, String relativePath) {
        DiskManagerFileInfo[] files = dm.getDiskManagerFileInfoSet().getFiles();
        boolean[] fileSelection = new boolean[files.length];
        for (int i = 0; i < files.length; i++) {
            fileSelection[i] = files[i].getFile(false).getAbsolutePath().endsWith(relativePath);
        }

        return fileSelection;
    }

    private static DownloadManager createDownloadManager(TransferManager manager, String torrentFile, final boolean[] fileSelection) {
        GlobalManager globalManager = AzureusManager.instance().getGlobalManager();
        String saveDir = SystemUtils.getTorrentDataDirectory().getAbsolutePath();

        if (fileSelection == null) {
            return globalManager.addDownloadManager(torrentFile, null, saveDir, DownloadManager.STATE_WAITING, true, false, null);
        } else {
            return globalManager.addDownloadManager(torrentFile, null, saveDir, null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
                public void initialised(DownloadManager dm) {
                    setupPartialSelection(dm, fileSelection);
                }
            });
        }
    }
}
