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

package com.frostwire.android.gui.services;

import java.io.File;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.frostwire.android.core.CoreRuntimeException;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.core.messages.FrostWireMessage;
import com.frostwire.android.gui.services.EngineService.EngineServiceBinder;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class Engine implements IEngineService {

    private IEngineService service;

    private static Engine instance;

    public synchronized static void create(Application context) {
        if (instance != null) {
            return;
        }
        instance = new Engine(context);
    }

    public static Engine instance() {
        if (instance == null) {
            throw new CoreRuntimeException("Engine not created");
        }
        return instance;
    }

    private Engine(Application context) {
        startEngineService(context);
    }

    public byte getState() {
        return service != null ? service.getState() : IEngineService.STATE_INVALID;
    }

    public boolean isStarted() {
        return service != null ? service.isStarted() : false;
    }

    public boolean isStarting() {
        return service != null ? service.isStarting() : false;
    }

    public boolean isStopped() {
        return service != null ? service.isStopped() : false;
    }

    public boolean isStopping() {
        return service != null ? service.isStopping() : false;
    }

    public boolean isDisconnected() {
        return service != null ? service.isDisconnected() : false;
    }

    public void startServices() {
        if (service != null) {
            service.startServices();
        }
    }

    public void stopServices(boolean disconnected) {
        if (service != null) {
            service.stopServices(disconnected);
        }
    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return service != null ? service.getMediaPlayer() : null;
    }

    public FileDescriptor getMediaFD() {
        return service != null ? service.getMediaFD() : null;
    }

    @Override
    public void playMedia(FileDescriptor fd) {
        if (service != null) {
            service.playMedia(fd);
        }
    }

    @Override
    public void pauseMedia() {
        if (service != null) {
            service.pauseMedia();
        }
    }

    @Override
    public void stopMedia() {
        if (service != null) {
            service.stopMedia();
        }
    }

    public void sendMessage(FrostWireMessage message) {
        if (service != null) {
            service.sendMessage(message);
        }
    }

    public ThreadPool getThreadPool() {
        return service != null ? service.getThreadPool() : null;
    }

    public void notifyDownloadFinished(String displayName, File file) {
        if (service != null) {
            service.notifyDownloadFinished(displayName, file);
        }
    }

    /**
     * 
     * @param context This must be the application context, otherwise there will be a leak.
     */
    private void startEngineService(final Context context) {
        Intent i = new Intent();
        i.setClass(context, EngineService.class);
        context.startService(i);
        context.bindService(i, new ServiceConnection() {
            public void onServiceDisconnected(ComponentName name) {
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                Engine.this.service = ((EngineServiceBinder) service).getService();

                registerStatusReceiver(context);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private void registerStatusReceiver(Context context) {
        EngineBroadcastReceiver receiver = new EngineBroadcastReceiver();

        IntentFilter wifiFilter = new IntentFilter();

        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);

        IntentFilter fileFilter = new IntentFilter();

        fileFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        fileFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        fileFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        fileFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        fileFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        fileFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        fileFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        fileFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        fileFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        fileFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        fileFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        fileFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        fileFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        fileFilter.addAction(Intent.ACTION_UMS_CONNECTED);
        fileFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
        fileFilter.addDataScheme("file");

        IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter audioFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addDataScheme("package");

        context.registerReceiver(receiver, wifiFilter);
        context.registerReceiver(receiver, fileFilter);
        context.registerReceiver(receiver, connectivityFilter);
        context.registerReceiver(receiver, audioFilter);
        context.registerReceiver(receiver, packageFilter);
    }
}
