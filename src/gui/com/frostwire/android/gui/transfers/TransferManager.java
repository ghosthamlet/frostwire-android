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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrentException;

import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.gui.Peer;
import com.frostwire.android.gui.search.BittorrentSearchResult;
import com.frostwire.android.gui.search.SearchResult;
import com.frostwire.android.util.ByteUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class TransferManager {

    private static final String TAG = "FW.TransferManager";

    private final List<PeerHttpDownload> peerDownloads;
    private final List<PeerHttpUpload> peerUploads;
    private final List<BittorrentDownload> bittorrenDownloads;

    private int downloadsToReview;

    private static TransferManager instance;

    public static TransferManager instance() {
        if (instance == null) {
            instance = new TransferManager();
        }
        return instance;
    }

    private TransferManager() {
        this.peerDownloads = new LinkedList<PeerHttpDownload>();
        this.peerUploads = new LinkedList<PeerHttpUpload>();
        this.bittorrenDownloads = new LinkedList<BittorrentDownload>();

        this.downloadsToReview = 0;

        loadTorrents();
    }

    public List<Transfer> getTransfers() {
        List<Transfer> transfers = new ArrayList<Transfer>();

        transfers.addAll(peerDownloads);
        transfers.addAll(peerUploads);
        transfers.addAll(bittorrenDownloads);

        return transfers;
    }

    public DownloadTransfer download(SearchResult sr) throws Exception {
        if (sr instanceof BittorrentSearchResult) {
            return newBittorrentDownload((BittorrentSearchResult) sr);
        } else {
            return new InvalidDownload();
        }
    }

    public void download(Peer peer, FileDescriptor fd) {
        PeerHttpDownload download = new PeerHttpDownload(this, peer, fd);

        peerDownloads.add(download);

        download.start();
    }

    public PeerHttpUpload upload(FileDescriptor fd) {
        PeerHttpUpload upload = new PeerHttpUpload(this, fd);
        peerUploads.add(upload);
        return upload;
    }

    public void clearComplete() {
        List<Transfer> transfers = getTransfers();

        for (Transfer transfer : transfers) {
            if (transfer.isComplete()) {
                if (transfer instanceof BittorrentDownload) {
                    BittorrentDownload bd = (BittorrentDownload) transfer;
                    if (bd.isResumable()) {
                        bd.cancel();
                    }
                } else {
                    transfer.cancel();
                }
            }
        }
    }

    public int getActiveDownloads() {
        int count = 0;

        for (BittorrentDownload d : bittorrenDownloads) {
            if (!d.isComplete() && d.isDownloading()) {
                count++;
            }
        }

        for (PeerHttpDownload d : peerDownloads) {
            if (!d.isComplete() && d.isDownloading()) {
                count++;
            }
        }

        return count;
    }

    public int getActiveUploads() {
        int count = 0;

        for (BittorrentDownload d : bittorrenDownloads) {
            if (!d.isComplete() && d.isSeeding()) {
                count++;
            }
        }

        for (PeerHttpUpload u : peerUploads) {
            if (!u.isComplete() && u.isUploading()) {
                count++;
            }
        }

        return count;
    }

    public long getDownloadsBandwidth() {
        long torrenDownloadsBandwidth = AzureusManager.instance().getGlobalManager().getStats().getDataReceiveRate() / 1000;

        long peerDownloadsBandwidth = 0;
        for (PeerHttpDownload d : peerDownloads) {
            peerDownloadsBandwidth += d.getDownloadSpeed() / 1000;
        }

        return torrenDownloadsBandwidth + peerDownloadsBandwidth;
    }

    public double getUploadsBandwidth() {
        long torrenUploadsBandwidth = AzureusManager.instance().getGlobalManager().getStats().getDataSendRate() / 1000;

        long peerUploadsBandwidth = 0;
        for (PeerHttpUpload u : peerUploads) {
            peerUploadsBandwidth += u.getUploadSpeed() / 1000;
        }

        return torrenUploadsBandwidth + peerUploadsBandwidth;
    }

    public int getDownloadsToReview() {
        return downloadsToReview;
    }

    public void incrementDownloadsToReview() {
        downloadsToReview++;
    }

    public void clearDownloadsToReview() {
        downloadsToReview = 0;
    }

    public void stopSeedingTorrents() {
        for (BittorrentDownload d : bittorrenDownloads) {
            if (d.isSeeding() || d.isComplete()) {
                d.pause();
            }
        }
    }

    List<BittorrentDownload> getBittorrentDownloads() {
        return new LinkedList<BittorrentDownload>(bittorrenDownloads);
    }

    void remove(Transfer transfer) {
        if (transfer instanceof BittorrentDownload) {
            bittorrenDownloads.remove(transfer);
        } else if (transfer instanceof PeerHttpDownload) {
            peerDownloads.remove(transfer);
        } else if (transfer instanceof PeerHttpUpload) {
            peerUploads.remove(transfer);
        }
    }

    private void loadTorrents() {
        GlobalManager globalManager = AzureusManager.instance().getAzureusCore().getGlobalManager();
        List<?> downloadManagers = globalManager.getDownloadManagers();

        List<DownloadManager> downloads = new ArrayList<DownloadManager>();
        for (Object obj : downloadManagers) {
            if (obj instanceof DownloadManager) {
                downloads.add((DownloadManager) obj);
                try {
                    Log.d(TAG, "Loading torrent with hash: " + ByteUtils.encodeHex(((DownloadManager) obj).getTorrent().getHash()));
                } catch (TOTorrentException e) {
                    // ignore
                }
            }
        }

        boolean stop = false;
        if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS)) {
            stop = true;
        } else {
            if (!NetworkManager.instance().isDataWIFIUp() && ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY)) {
                stop = true;
            }
        }

        for (DownloadManager dm : downloads) {
            if (stop && TorrentUtil.isComplete(dm)) {
                TorrentUtil.stop(dm);
            }

            bittorrenDownloads.add(BittorrentDownloadCreator.create(this, dm));
        }
    }

    private BittorrentDownload newBittorrentDownload(BittorrentSearchResult sr) throws Exception {
        BittorrentDownload download = BittorrentDownloadCreator.create(this, sr);

        if (!(download instanceof InvalidBittorrentDownload)) {
            bittorrenDownloads.add(download);
        }

        return download;
    }

    public void pauseTorrents() {
        for (BittorrentDownload d : bittorrenDownloads) {
            d.pause();
        }
    }
}
