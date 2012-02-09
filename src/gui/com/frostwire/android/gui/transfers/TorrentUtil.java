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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AsyncDispatcher;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.sharing.ShareManager;
import org.gudy.azureus2.plugins.sharing.ShareResource;
import org.gudy.azureus2.plugins.sharing.ShareResourceDir;
import org.gudy.azureus2.plugins.sharing.ShareResourceFile;
import org.gudy.azureus2.plugins.tracker.Tracker;
import org.gudy.azureus2.plugins.tracker.TrackerTorrent;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;

import android.util.Log;

import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.ui.UIFunctions;
import com.aelitis.azureus.ui.UIFunctionsManager;
import com.frostwire.android.util.FileUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentUtil {

    private static final String TAG = "FW.TorrentUtil";

    private static AsyncDispatcher async = new AsyncDispatcher(2000);

    public static Set<DiskManagerFileInfo> getNoSkippedFileInfoSet(DownloadManager dm) {
        Set<DiskManagerFileInfo> set = new HashSet<DiskManagerFileInfo>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (!fileInfo.isSkipped()) {
                set.add(fileInfo);
            }
        }
        return set;
    }

    public static Set<File> getSkippedFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (fileInfo.isSkipped()) {
                set.add(fileInfo.getFile(false));
            }
        }
        return set;
    }

    public static Set<File> getIncompleteFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();

        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (getDownloadPercent(fileInfo) < 100) {
                set.add(fileInfo.getFile(false));
            }
        }

        return set;
    }

    public static boolean isStartable(DownloadManager dm) {
        if (dm == null)
            return false;
        int state = dm.getState();
        if (state != DownloadManager.STATE_STOPPED) {
            return false;
        }
        return true;
    }

    public static boolean isStopable(DownloadManager dm) {
        if (dm == null)
            return false;
        int state = dm.getState();
        if (state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_STOPPING) {
            return false;
        }
        return true;
    }

    public static boolean isComplete(DownloadManager dm) {
        /*
        if (!TorrentUtil.getSkippedFiles(dm).isEmpty()) {
            long downloaded = 0;
            long size = 0;
            for (DiskManagerFileInfo fileInfo : getNoSkippedFileInfoSet(dm)) {
                downloaded += fileInfo.getDownloaded();
                size += fileInfo.getLength();
            }
            return downloaded == size;
        } else {
            return dm.getStats().getDownloadCompleted(true) == 1000;
        }*/
        return dm.getAssumedComplete();
    }

    public static void removeDownload(DownloadManager downloadManager, boolean deleteTorrent, boolean deleteData) {
        removeDownload(downloadManager, deleteTorrent, deleteData, true);
    }

    public static void removeDownload(DownloadManager downloadManager, boolean deleteTorrent, boolean deleteData, boolean async) {
        if (async) {
            asyncStopDelete(downloadManager, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
        } else {
            blockingStopDelete(downloadManager, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
        }
    }

    public static void asyncStopDelete(final DownloadManager dm, final int stateAfterStopped, final boolean bDeleteTorrent, final boolean bDeleteData, final AERunnable deleteFailed) {

        async.dispatch(new AERunnable() {
            public void runSupport() {

                try {
                    // I would move the FLAG_DO_NOT_DELETE_DATA_ON_REMOVE even deeper
                    // but I fear what could possibly go wrong.
                    boolean reallyDeleteData = bDeleteData && !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);

                    dm.getGlobalManager().removeDownloadManager(dm, bDeleteTorrent, reallyDeleteData);
                } catch (GlobalManagerDownloadRemovalVetoException f) {

                    // see if we can delete a corresponding share as users frequently share
                    // stuff by mistake and then don't understand how to delete the share
                    // properly

                    try {
                        PluginInterface pi = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface();

                        ShareManager sm = pi.getShareManager();

                        Tracker tracker = pi.getTracker();

                        ShareResource[] shares = sm.getShares();

                        TOTorrent torrent = dm.getTorrent();

                        byte[] target_hash = torrent.getHash();

                        for (ShareResource share : shares) {

                            int type = share.getType();

                            byte[] hash;

                            if (type == ShareResource.ST_DIR) {

                                hash = ((ShareResourceDir) share).getItem().getTorrent().getHash();

                            } else if (type == ShareResource.ST_FILE) {

                                hash = ((ShareResourceFile) share).getItem().getTorrent().getHash();

                            } else {

                                hash = null;
                            }

                            if (hash != null) {

                                if (Arrays.equals(target_hash, hash)) {

                                    try {
                                        dm.stopIt(DownloadManager.STATE_STOPPED, false, false);

                                    } catch (Throwable e) {
                                    }

                                    try {
                                        TrackerTorrent tracker_torrent = tracker.getTorrent(PluginCoreUtils.wrap(torrent));

                                        if (tracker_torrent != null) {

                                            tracker_torrent.stop();
                                        }
                                    } catch (Throwable e) {
                                    }

                                    share.delete();

                                    return;
                                }
                            }
                        }

                    } catch (Throwable e) {

                    }

                    if (!f.isSilent()) {
                        UIFunctionsManager.getUIFunctions().forceNotify(UIFunctions.STATUSICON_WARNING, MessageText.getString("globalmanager.download.remove.veto"), f.getMessage(), null, null, -1);

                        //Logger.log(new LogAlert(dm, false, "{globalmanager.download.remove.veto}", f));
                    }
                    if (deleteFailed != null) {
                        deleteFailed.runSupport();
                    }
                } catch (Exception ex) {
                    Debug.printStackTrace(ex);
                    if (deleteFailed != null) {
                        deleteFailed.runSupport();
                    }
                }

                finalCleanup(dm);
            }
        });
    }

    public static void blockingStopDelete(final DownloadManager dm, final int stateAfterStopped, final boolean bDeleteTorrent, final boolean bDeleteData, final AERunnable deleteFailed) {

        try {
            // I would move the FLAG_DO_NOT_DELETE_DATA_ON_REMOVE even deeper
            // but I fear what could possibly go wrong.
            boolean reallyDeleteData = bDeleteData && !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);

            dm.getGlobalManager().removeDownloadManager(dm, bDeleteTorrent, reallyDeleteData);
        } catch (GlobalManagerDownloadRemovalVetoException f) {

            // see if we can delete a corresponding share as users frequently share
            // stuff by mistake and then don't understand how to delete the share
            // properly

            try {
                PluginInterface pi = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface();

                ShareManager sm = pi.getShareManager();

                Tracker tracker = pi.getTracker();

                ShareResource[] shares = sm.getShares();

                TOTorrent torrent = dm.getTorrent();

                byte[] target_hash = torrent.getHash();

                for (ShareResource share : shares) {

                    int type = share.getType();

                    byte[] hash;

                    if (type == ShareResource.ST_DIR) {

                        hash = ((ShareResourceDir) share).getItem().getTorrent().getHash();

                    } else if (type == ShareResource.ST_FILE) {

                        hash = ((ShareResourceFile) share).getItem().getTorrent().getHash();

                    } else {

                        hash = null;
                    }

                    if (hash != null) {

                        if (Arrays.equals(target_hash, hash)) {

                            try {
                                dm.stopIt(DownloadManager.STATE_STOPPED, false, false);

                            } catch (Throwable e) {
                            }

                            try {
                                TrackerTorrent tracker_torrent = tracker.getTorrent(PluginCoreUtils.wrap(torrent));

                                if (tracker_torrent != null) {

                                    tracker_torrent.stop();
                                }
                            } catch (Throwable e) {
                            }

                            share.delete();

                            return;
                        }
                    }
                }

            } catch (Throwable e) {

            }

            if (!f.isSilent()) {
                UIFunctionsManager.getUIFunctions().forceNotify(UIFunctions.STATUSICON_WARNING, MessageText.getString("globalmanager.download.remove.veto"), f.getMessage(), null, null, -1);

                // Logger.log(new LogAlert(dm, false,
                // "{globalmanager.download.remove.veto}", f));
            }
            if (deleteFailed != null) {
                deleteFailed.runSupport();
            }
        } catch (Exception ex) {
            Debug.printStackTrace(ex);
            if (deleteFailed != null) {
                deleteFailed.runSupport();
            }
        }

        finalCleanup(dm);
    }

    public static void start(DownloadManager dm) {
        if (dm != null && dm.getState() == DownloadManager.STATE_STOPPED) {
            dm.initialize();
        }
    }

    public static void stop(DownloadManager dm) {
        stop(dm, DownloadManager.STATE_STOPPED);
    }

    public static void stop(final DownloadManager dm, final int stateAfterStopped) {
        if (dm == null) {
            return;
        }

        int state = dm.getState();

        if (state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_STOPPING || state == stateAfterStopped) {
            return;
        }

        asyncStop(dm, stateAfterStopped);
    }

    public static void asyncStop(final DownloadManager dm, final int stateAfterStopped) {
        async.dispatch(new AERunnable() {
            public void runSupport() {
                dm.stopIt(stateAfterStopped, false, false);
            }
        });
    }

    public static String hashToString(byte[] hash) {
        String hex = "";
        for (int i = 0; i < hash.length; i++) {
            String t = Integer.toHexString(hash[i] & 0xFF);
            if (t.length() < 2) {
                t = "0" + t;
            }
            hex += t;
        }

        return hex;
    }

    public static Set<File> getIgnorableFiles() {
        Set<File> set = TorrentUtil.getIncompleteFiles();
        set.addAll(TorrentUtil.getSkipedFiles());
        return set;
    }

    public static Set<File> getIncompleteFiles() {
        Set<File> set = new HashSet<File>();

        if (!AzureusManager.isCreated()) {
            return set;
        }

        List<?> dms = AzureusManager.instance().getGlobalManager().getDownloadManagers();
        for (Object obj : dms) {
            DownloadManager dm = (DownloadManager) obj;
            set.addAll(getIncompleteFiles(dm));
        }

        return set;
    }

    public static int getDownloadPercent(DiskManagerFileInfo fileInfo) {
        long length = fileInfo.getLength();
        if (length == 0 || fileInfo.getDownloaded() == length) {
            return 100;
        } else {
            return (int) (fileInfo.getDownloaded() * 100 / length);
        }
    }

    public static Set<File> getSkipedFiles() {
        Set<File> set = new HashSet<File>();

        if (!AzureusManager.isCreated()) {
            return set;
        }

        List<?> dms = AzureusManager.instance().getGlobalManager().getDownloadManagers();
        for (Object obj : dms) {
            DownloadManager dm = (DownloadManager) obj;
            set.addAll(getSkippedFiles(dm));
        }

        return set;
    }

    /**
     * Deletes incomplete and skipped files.
     */
    private static void finalCleanup(DownloadManager dm) {
        Set<File> filesToDelete = getSkippedFiles(dm);
        filesToDelete.addAll(getIncompleteFiles(dm));

        for (File f : filesToDelete) {
            try {
                if (f.exists() && !f.delete()) {
                    Log.w(TAG, "Can't delete file: " + f);
                }
            } catch (Throwable e) {
                Log.e(TAG, "Can't delete file: " + f, e);
            }
        }

        FileUtils.deleteEmptyDirectoryRecursive(dm.getSaveLocation());
    }
}
