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

import android.media.MediaPlayer;

import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.core.messages.FrostWireMessage;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public interface IEngineService {

    public static final byte STATE_INVALID = -1;
    public static final byte STATE_STARTED = 10;
    public static final byte STATE_STARTING = 11;
    public static final byte STATE_STOPPED = 12;
    public static final byte STATE_STOPPING = 13;
    public static final byte STATE_DISCONNECTED = 14;

    public MediaPlayer getMediaPlayer();

    /**
     * The current file the media player is playing.
     * 
     * @return
     */
    public FileDescriptor getMediaFD();

    public void playMedia(FileDescriptor fd);

    public void pauseMedia();

    public void stopMedia();

    public byte getState();

    public boolean isStarted();

    public boolean isStarting();

    public boolean isStopped();

    public boolean isStopping();

    public boolean isDisconnected();

    public void startServices();

    public void stopServices(boolean disconnected);

    public void sendMessage(FrostWireMessage message);

    public ThreadPool getThreadPool();

    public void notifyDownloadFinished(String displayName, File file);
}
