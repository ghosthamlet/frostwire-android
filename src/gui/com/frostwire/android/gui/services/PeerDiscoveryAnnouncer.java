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

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.messages.PingMessage;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.util.concurrent.ExtendedRunnable;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * This class creates periodical PingMessages and gives them to the MessageClerk
 * for delivery.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class PeerDiscoveryAnnouncer implements ExtendedRunnable {

    @SuppressWarnings("unused")
    private static final String TAG = "FW.PeerDiscoveryAnnouncer";

    private final byte[] uuid;
    private final ThreadPool threadPool;

    private boolean running;

    public PeerDiscoveryAnnouncer(ThreadPool threadPool) {
        this.uuid = ConfigurationManager.instance().getUUID();
        this.threadPool = threadPool;
    }

    public String getName() {
        return "PeerDiscoveryAnnouncer";
    }

    public void run() {
        int pingsInterval = ConfigurationManager.instance().getInt(Constants.PREF_KEY_NETWORK_PINGS_INTERVAL);

        while (running) {
            sendLocalBroadcastPingMessage(false);
            sleep(pingsInterval);
        }
    }

    public void start() {
        running = true;
        threadPool.execute(this);
    }

    public synchronized void stop() {
        running = false;
        notify();
        sayGoodByes();
    }

    private void sendLocalBroadcastPingMessage(boolean bye) {
        PingMessage message = createPingMessage(NetworkManager.instance().getListeningPort(), bye, uuid);
        Engine.instance().sendMessage(message);
    }

    private PingMessage createPingMessage(int listeningPort, boolean bye, byte[] uuid) {
        String nickname = ConfigurationManager.instance().getNickname();
        int numSharedFiles = Librarian.instance().getNumFiles();

        PingMessage message = new PingMessage(listeningPort, numSharedFiles, nickname, bye);
        message.getHeader().setUUID(uuid);

        return message;
    }

    /**
     * Send Ping-GoodBye messages to Local network (broadcast || multicast)
     */
    private void sayGoodByes() {
        // say good bye on local network
        sendLocalBroadcastPingMessage(true);
    }

    private synchronized void sleep(long delay) {
        try {
            wait(delay);
        } catch (InterruptedException e) {
        }
    }
}