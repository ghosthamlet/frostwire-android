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

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.frostwire.android.util.concurrent.AbstractRunnable;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
abstract class SocketClerk<S> extends AbstractRunnable {

    private S socket;
    private ThreadPool threadPool;

    public SocketClerk(String name, S socket, ThreadPool threadPool) {
        super(name);

        if (!(socket instanceof DatagramSocket)) {
            throw new IllegalArgumentException("Socket type not supported");
        }

        this.socket = socket;
        this.threadPool = threadPool;
    }

    public void start() {
        threadPool.execute(this);
    }

    public void stop() {
        closeSocket();
    }

    public void run() {
        if (socket instanceof DatagramSocket) {
            runUDPHelper();
        }
    }

    protected abstract void onDataReceived(InetAddress origin, byte[] data, int length);

    protected abstract boolean isProcessing();

    private void runUDPHelper() {
        DatagramSocket socket = (DatagramSocket) this.socket;

        try {
            byte[] data = new byte[65535];

            while (isProcessing()) {
                try {

                    DatagramPacket packet = new DatagramPacket(data, data.length);

                    socket.receive(packet);

                    onDataReceived(packet.getAddress(), data, packet.getLength());

                } catch (InterruptedIOException e) {
                    if (!isProcessing()) {
                        break;
                    }
                }
            }

        } catch (Throwable e) {
            // ignore
        } finally {
            closeSocket();
        }
    }

    private void closeSocket() {
        try {
            if (socket instanceof DatagramSocket) {
                ((DatagramSocket) socket).close();
            }
        } catch (Throwable e) {
            // ignore
        }
    }
}
