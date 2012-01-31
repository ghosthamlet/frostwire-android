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
class PeerDiscoveryAnnouncer implements ExtendedRunnable {

    private final ThreadPool threadPool;

    private boolean running;

    public PeerDiscoveryAnnouncer(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public String getName() {
        return "PeerDiscoveryAnnouncer";
    }

    public void run() {
        byte[] uuid = ConfigurationManager.instance().getUUID();
        int pingsInterval = ConfigurationManager.instance().getInt(Constants.PREF_KEY_NETWORK_PINGS_INTERVAL);

        while (running) {
            PingMessage message = createPingMessage(NetworkManager.instance().getListeningPort(), false, uuid);
            Engine.instance().sendMessage(message);

            sleep(pingsInterval);
        }
    }

    public void start() {
        if (!NetworkManager.instance().isDataWIFIUp()) {
            return;
        }

        running = true;
        threadPool.execute(this);
    }

    public synchronized void stop() {
        running = false;
        notify();
    }

    static PingMessage createPingMessage(int listeningPort, boolean bye, byte[] uuid) {
        int numSharedFiles = Librarian.instance().getNumFiles();
        String nickname = ConfigurationManager.instance().getNickname();

        PingMessage ping = new PingMessage(listeningPort, numSharedFiles, nickname, bye);
        ping.getHeader().setUUID(uuid);

        return ping;
    }

    private synchronized void sleep(long delay) {
        try {
            wait(delay);
        } catch (InterruptedException e) {
        }
    }
}