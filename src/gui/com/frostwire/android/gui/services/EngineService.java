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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.core.messages.FrostWireMessage;
import com.frostwire.android.core.messages.PingMessage;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.gui.PeerManager;
import com.frostwire.android.gui.activities.MainActivity;
import com.frostwire.android.gui.activities.MediaPlayerActivity;
import com.frostwire.android.gui.httpserver.HttpServerManager;
import com.frostwire.android.gui.transfers.AzureusManager;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class EngineService extends Service implements IEngineService, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "FW.EngineService";

    private final static long[] VENEZUELAN_VIBE = buildVenezuelanVibe();

    private final IBinder binder;

    private MediaPlayer mediaPlayer;
    private FileDescriptor mediaFD;

    private final ThreadPool threadPool;

    // services in background
    private final HttpServerManager httpServerManager;

    private final MessageProcessor messageProcessor;
    private final MessageClerk messageClerk;
    private final MessageCourier messageCourier;

    private final PeerDiscoveryAnnouncer peerDiscoveryAnnouncer;

    private byte state;

    private OnSharedPreferenceChangeListener preferenceListener;

    public EngineService() {
        binder = new EngineServiceBinder();

        threadPool = new ThreadPool("Engine");

        httpServerManager = new HttpServerManager(threadPool);

        messageProcessor = new MessageProcessor(threadPool);
        messageClerk = new MessageClerk(threadPool, messageProcessor);
        messageCourier = new MessageCourier(threadPool);

        peerDiscoveryAnnouncer = new PeerDiscoveryAnnouncer(threadPool);

        registerPreferencesChangeListener();

        state = STATE_DISCONNECTED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        stopServices(false);
        stopMedia();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public FileDescriptor getMediaFD() {
        return mediaFD;
    }

    @Override
    public void playMedia(FileDescriptor fd) {
        stopMedia();
        setupMediaPlayer();

        try {
            mediaPlayer.setDataSource(fd.filePath);
            mediaPlayer.prepareAsync();
            mediaFD = fd;
        } catch (Throwable e) {
            Log.e(TAG, "Error in playMedia", e);
            releaseMediaPlayer();
        }
    }

    @Override
    public void pauseMedia() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

    @Override
    public void stopMedia() {
        if (mediaPlayer != null) {
            stopForeground(true);
        }

        releaseMediaPlayer();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Constants.NOTIFICATION_MEDIA_PLAYING_ID);
        sendBroadcast(new Intent(Constants.ACTION_MEDIA_PLAYER_STOPPED));
    }

    public byte getState() {
        return state;
    }

    public boolean isStarted() {
        return getState() == STATE_STARTED;
    }

    public boolean isStarting() {
        return getState() == STATE_STARTING;
    }

    public boolean isStopped() {
        return getState() == STATE_STOPPED;
    }

    public boolean isStopping() {
        return getState() == STATE_STOPPING;
    }

    public boolean isDisconnected() {
        return getState() == STATE_DISCONNECTED;
    }

    public synchronized void startServices() {
        // hard check for TOS
        if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_TOS_ACCEPTED)) {
            return;
        }

        if (isStarted() || isStarting()) {
            return;
        }

        state = STATE_STARTING;

        AzureusManager.instance().resume();

        httpServerManager.start(NetworkManager.instance().getListeningPort());

        messageProcessor.startProcessing();
        messageClerk.startProcessing();
        messageCourier.startProcessing();

        peerDiscoveryAnnouncer.start();

        PeerManager.instance().clear();

        state = STATE_STARTED;
        Log.v(TAG, "Engine started");
    }

    public synchronized void stopServices(boolean disconnected) {
        if (isStopped() || isStopping() || isDisconnected()) {
            return;
        }

        state = STATE_STOPPING;

        sendGoodByes();

        peerDiscoveryAnnouncer.stop();

        messageCourier.stopProcessing();
        messageClerk.stopProcessing();
        messageProcessor.stopProcessing();

        httpServerManager.stop();

        AzureusManager.instance().pause();

        PeerManager.instance().clear();

        state = disconnected ? STATE_DISCONNECTED : STATE_STOPPED;
        Log.v(TAG, "Engine stopped, state: " + state);
    }

    public void sendMessage(FrostWireMessage message) {
        messageCourier.addElement(message);
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void notifyDownloadFinished(String displayName, File file) {
        try {
            Context context = getApplicationContext();

            Intent i = new Intent(context, MainActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra(Constants.EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION, true);
            i.putExtra(Constants.EXTRA_DOWNLOAD_COMPLETE_PATH, file.getAbsolutePath());

            PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.frostwire_notification, getString(R.string.download_finished), System.currentTimeMillis());
            notification.vibrate = ConfigurationManager.instance().vibrateOnFinishedDownload() ? VENEZUELAN_VIBE : null;
            notification.number = TransferManager.instance().getDownloadsToReview();
            notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
            notification.setLatestEventInfo(context, getString(R.string.download_finished), displayName, pi);
            manager.notify(Constants.NOTIFICATION_DOWNLOAD_TRANSFER_FINISHED, notification);
        } catch (Throwable e) {
            Log.e(TAG, "Error creating notification for download finished", e);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mediaPlayer != null) {
            mediaPlayer.start();

            notifyMediaPlay();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        UIUtils.showShortMessage(this, R.string.media_player_failed);

        releaseMediaPlayer();
        stopForeground(true);

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
    }

    private void setupMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = null;
        mediaFD = null;
    }

    private void registerPreferencesChangeListener() {
        preferenceListener = new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Constants.PREF_KEY_GUI_NICKNAME)) {
                    PeerManager.instance().clear();
                } else if (key.equals(Constants.PREF_KEY_NETWORK_USE_MULTICAST) || key.equals(Constants.PREF_KEY_NETWORK_USE_BROADCAST)) {
                    resetLocalNetworkProcessors();
                }
            }
        };
        ConfigurationManager.instance().registerOnPreferenceChange(preferenceListener);
    }

    private void resetLocalNetworkProcessors() {
        Log.d(TAG, "Restarting courier and clerk");
        messageCourier.stopProcessing();
        messageClerk.stopProcessing();

        messageClerk.startProcessing();
        messageCourier.startProcessing();
    }

    private void notifyMediaPlay() {
        Context context = getApplicationContext();

        Intent i = new Intent(context, MediaPlayerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification();
        notification.tickerText = getString(R.string.playing_song_name, mediaFD.title);
        notification.icon = R.drawable.play_notification;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(context, getString(R.string.application_label), getString(R.string.playing_song_name, mediaFD.title), pi);
        startForeground(Constants.NOTIFICATION_MEDIA_PLAYING_ID, notification);
    }

    private static long[] buildVenezuelanVibe() {

        long shortVibration = 80;
        long mediumVibration = 100;
        long shortPause = 100;
        long mediumPause = 150;
        long longPause = 180;

        return new long[] { 0, shortVibration, longPause, shortVibration, shortPause, shortVibration, shortPause, shortVibration, mediumPause, mediumVibration };
    }

    /**
     * Send Ping-GoodBye messages to Local network (broadcast || multicast)
     */
    private void sendGoodByes() {
        PingMessage ping = PeerDiscoveryAnnouncer.createPingMessage(NetworkManager.instance().getListeningPort(), true, ConfigurationManager.instance().getUUID());
        try {
            Log.d(TAG, "Sending good-byes");
            messageCourier.processElement(ping);
        } catch (Throwable e) {
            Log.e(TAG, "Unable to send good-byes");
        }
    }

    public class EngineServiceBinder extends Binder {
        public IEngineService getService() {
            return EngineService.this;
        }
    }
}
