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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.core3.util.SystemProperties;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreException;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreLifecycleAdapter;
import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.CoreRuntimeException;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.gui.util.SystemUtils;

/**
 * Class to initialize the azureus core.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class AzureusManager {

    private static final String TAG = "FW.AzureusManager";

    private static final String AZUREUS_CONFIG_KEY_MAX_DOWNLOAD_SPEED = "Max Download Speed KBs";
    private static final String AZUREUS_CONFIG_KEY_MAX_UPLOAD_SPEED = "Max Upload Speed KBs";
    private static final String AZUREUS_CONFIG_KEY_MAX_DOWNLOADS = "max downloads";
    private static final String AZUREUS_CONFIG_KEY_MAX_UPLOADS = "Max Uploads";
    private static final String AZUREUS_CONFIG_KEY_MAX_TOTAL_CONNECTIONS = "Max.Peer.Connections.Total";
    private static final String AZUREUS_CONFIG_KEY_MAX_TORRENT_CONNECTIONS = "Max.Peer.Connections.Per.Torrent";

    private AzureusCore azureusCore;

    private OnSharedPreferenceChangeListener preferenceListener;

    private static AzureusManager instance;

    public synchronized static boolean isCreated() {
        return instance != null;
    }

    public synchronized static void create(Context context) {
        if (!Librarian.instance().isExternalStorageMounted() || instance != null) {
            return;
        }
        instance = new AzureusManager(context);
    }

    public static AzureusManager instance() {
        if (instance == null) {
            throw new CoreRuntimeException("AzureusManager not created");
        }
        return instance;
    }

    private AzureusManager(Context context) {
        azureusConfigurationInit();
        loadMessages(context);
        azureusInit();
        azureusStart();
    }

    public void pause() {
        try {
            SimpleTimer.pause();
            azureusCore.getGlobalManager().pauseDownloads();
        } catch (Throwable e) {
            Log.d(TAG, "Failed to pause Azureus core", e);
        }
    }

    public void resume() {
        try {
            COConfigurationManager.setParameter("UDP.Listen.Port.Enable", NetworkManager.instance().isDataWIFIUp());
            COConfigurationManager.save();

            SimpleTimer.resume();
            azureusCore.getGlobalManager().resumeDownloads();
        } catch (Throwable e) {
            Log.d(TAG, "Failed to resume Azureus core", e);
        }
    }

    public static void revertToDefaultConfiguration() {
        COConfigurationManager.resetToDefaults();
        autoAdjustBittorrentSpeed();
    }

    public static void autoAdjustBittorrentSpeed() {
        if (COConfigurationManager.getBooleanParameter("Auto Adjust Transfer Defaults")) {

            int up_limit_bytes_per_sec = 0;//getEstimatedUploadCapacityBytesPerSec().getBytesPerSec();
            //int down_limit_bytes_per_sec    = 0;//getEstimatedDownloadCapacityBytesPerSec().getBytesPerSec();

            int up_kbs = up_limit_bytes_per_sec / 1024;

            final int[][] settings = {

            { 56, 2, 20, 40 }, // 56 k/bit
                    { 96, 3, 30, 60 }, { 128, 3, 40, 80 }, { 192, 4, 50, 100 }, // currently we don't go lower than this
                    { 256, 4, 60, 200 }, { 512, 5, 70, 300 }, { 1024, 6, 80, 400 }, // 1Mbit
                    { 2 * 1024, 8, 90, 500 }, { 5 * 1024, 10, 100, 600 }, { 10 * 1024, 20, 110, 750 }, // 10Mbit
                    { 20 * 1024, 30, 120, 900 }, { 50 * 1024, 40, 130, 1100 }, { 100 * 1024, 50, 140, 1300 }, { -1, 60, 150, 1500 }, };

            int[] selected = settings[settings.length - 1];

            // note, we start from 3 to avoid over-restricting things when we don't have
            // a reasonable speed estimate

            for (int i = 3; i < settings.length; i++) {

                int[] setting = settings[i];

                int line_kilobit_sec = setting[0];

                // convert to upload kbyte/sec assuming 80% achieved

                int limit = (line_kilobit_sec / 8) * 4 / 5;

                if (up_kbs <= limit) {

                    selected = setting;

                    break;
                }
            }

            int upload_slots = selected[1];
            int connections_torrent = selected[2];
            int connections_global = selected[3];

            if (upload_slots != COConfigurationManager.getIntParameter("Max Uploads")) {
                COConfigurationManager.setParameter("Max Uploads", upload_slots);
                COConfigurationManager.setParameter("Max Uploads Seeding", upload_slots);
            }

            if (connections_torrent != COConfigurationManager.getIntParameter("Max.Peer.Connections.Per.Torrent")) {
                COConfigurationManager.setParameter("Max.Peer.Connections.Per.Torrent", connections_torrent);
                COConfigurationManager.setParameter("Max.Peer.Connections.Per.Torrent.When.Seeding", connections_torrent / 2);
            }

            if (connections_global != COConfigurationManager.getIntParameter("Max.Peer.Connections.Total")) {
                COConfigurationManager.setParameter("Max.Peer.Connections.Total", connections_global);
            }
        }
    }

    AzureusCore getAzureusCore() {
        return azureusCore;
    }

    GlobalManager getGlobalManager() {
        return getAzureusCore().getGlobalManager();
    }

    private void azureusConfigurationInit() {
        File azureusPath = SystemUtils.getAzureusDirectory();

        System.setProperty("azureus.config.path", azureusPath.getAbsolutePath());
        System.setProperty("azureus.install.path", azureusPath.getAbsolutePath());
        System.setProperty("azureus.loadplugins", "0"); // disable third party azureus plugins

        SystemProperties.APPLICATION_NAME = "azureus";
        SystemProperties.setUserPath(azureusPath.getAbsolutePath());

        COConfigurationManager.setParameter("Auto Adjust Transfer Defaults", false);
        COConfigurationManager.setParameter("General_sDefaultTorrent_Directory", SystemUtils.getTorrentsDirectory().getAbsolutePath());

        // network parameters, fine tunning for android
        COConfigurationManager.setParameter("network.tcp.write.select.time", 1000);
        COConfigurationManager.setParameter("network.tcp.write.select.min.time", 1000);
        COConfigurationManager.setParameter("network.tcp.read.select.time", 1000);
        COConfigurationManager.setParameter("network.tcp.read.select.min.time", 1000);
        COConfigurationManager.setParameter("network.control.write.idle.time", 1000);
        COConfigurationManager.setParameter("network.control.read.idle.time", 1000);

        COConfigurationManager.save();
    }

    private void azureusInit() {
        try {
            azureusCore = AzureusCoreFactory.create();
        } catch (AzureusCoreException coreException) {
            //so we already had one ...
            if (azureusCore == null) {
                azureusCore = AzureusCoreFactory.getSingleton();
            }
        }

        registerPreferencesChangeListener();
    }

    private void azureusStart() {
        try {
            if (azureusCore.isStarted()) {
                Log.d(TAG, "Azureus core already started. skipping.");
                return;
            }

            final CountDownLatch signal = new CountDownLatch(1);
            azureusCore.addLifecycleListener(new AzureusCoreLifecycleAdapter() {
                @Override
                public void started(AzureusCore core) {
                    if (signal != null) {
                        signal.countDown();
                    }
                }
            });

            azureusCore.start();

            azureusCore.getGlobalManager().resumeDownloads();

            Log.d(TAG, "Azureus core starting...");
            try {
                signal.await();
                Log.d(TAG, "Azureus core started");
            } catch (InterruptedException e) {
                // ignore
            }
        } catch (Throwable e) {
            Log.d(TAG, "Failed to start Azureus core started", e);
        }
    }

    private void registerPreferencesChangeListener() {
        preferenceListener = new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Constants.PREF_KEY_TORRENT_MAX_DOWNLOAD_SPEED)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_DOWNLOAD_SPEED);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_UPLOAD_SPEED)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_UPLOAD_SPEED);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_DOWNLOADS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_DOWNLOADS);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_UPLOADS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_UPLOADS);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_TOTAL_CONNECTIONS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_TOTAL_CONNECTIONS);
                } else if (key.equals(Constants.PREF_KEY_TORRENT_MAX_TORRENT_CONNECTIONS)) {
                    setAzureusParameter(AZUREUS_CONFIG_KEY_MAX_TORRENT_CONNECTIONS);
                }
            }
        };
        ConfigurationManager.instance().registerOnPreferenceChange(preferenceListener);
    }

    private void setAzureusParameter(String key) {
        COConfigurationManager.setParameter(key, ConfigurationManager.instance().getLong(key));
        COConfigurationManager.save();
    }

    private void loadMessages(Context context) {
        Map<String, String> map = new HashMap<String, String>();

        map.put("PeerManager.status.finished", context.getString(R.string.azureus_peer_manager_status_finished));
        map.put("PeerManager.status.finishedin", context.getString(R.string.azureus_peer_manager_status_finishedin));
        map.put("Formats.units.alot", context.getString(R.string.azureus_formats_units_alot));
        map.put("discarded", context.getString(R.string.azureus_discarded));
        map.put("ManagerItem.waiting", context.getString(R.string.azureus_manager_item_waiting));
        map.put("ManagerItem.initializing", context.getString(R.string.azureus_manager_item_initializing));
        map.put("ManagerItem.allocating", context.getString(R.string.azureus_manager_item_allocating));
        map.put("ManagerItem.checking", context.getString(R.string.azureus_manager_item_checking));
        map.put("ManagerItem.finishing", context.getString(R.string.azureus_manager_item_finishing));
        map.put("ManagerItem.ready", context.getString(R.string.azureus_manager_item_ready));
        map.put("ManagerItem.downloading", context.getString(R.string.azureus_manager_item_downloading));
        map.put("ManagerItem.seeding", context.getString(R.string.azureus_manager_item_seeding));
        map.put("ManagerItem.superseeding", context.getString(R.string.azureus_manager_item_superseeding));
        map.put("ManagerItem.stopping", context.getString(R.string.azureus_manager_item_stopping));
        map.put("ManagerItem.stopped", context.getString(R.string.azureus_manager_item_stopped));
        map.put("ManagerItem.paused", context.getString(R.string.azureus_manager_item_paused));
        map.put("ManagerItem.queued", context.getString(R.string.azureus_manager_item_queued));
        map.put("ManagerItem.error", context.getString(R.string.azureus_manager_item_error));
        map.put("ManagerItem.forced", context.getString(R.string.azureus_manager_item_forced));
        map.put("GeneralView.yes", context.getString(R.string.azureus_general_view_yes));
        map.put("GeneralView.no", context.getString(R.string.azureus_general_view_no));

        DisplayFormatters.loadMessages(map);
    }
}
