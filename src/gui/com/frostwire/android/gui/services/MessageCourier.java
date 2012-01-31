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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.messages.FrostWireMessage;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * This class is responsible for SENDING all messages handed to it. If you need
 * a message sent, you just add it to the MessageCourier.
 * 
 * PingMessages are sent to both multicast and broadcast addresses. 
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class MessageCourier extends QueueProcessor<FrostWireMessage> {

    private static final String TAG = "FW.MessageCourier";

    private DatagramSocket broadcastSocket;
    private MulticastSocket multicastSocket;

    private InetSocketAddress broadcastAddress;
    private InetSocketAddress multicastAddress;

    private boolean useBroadcast;
    private boolean useMulticast;

    public MessageCourier(ThreadPool pool) {
        super("MessageCourier", pool, Constants.MESSAGE_COURIER_CAPACITY);
    }

    @Override
    public void processElement(FrostWireMessage elem) throws Exception {
        elem.setUUID(ConfigurationManager.instance().getUUID());
        if (elem.getType() == Constants.MESSAGE_TYPE_PING) {
            sendBroadcastMessage(elem, useBroadcast, useMulticast);
        }
    }

    @Override
    public void startProcessing() {
        if (NetworkManager.instance().isDataWIFIUp()) {
            super.startProcessing();
        }
    }

    @Override
    protected void onStartProcessing() {
        try {
            useBroadcast = ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_BROADCAST);
            useMulticast = ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_MULTICAST);

            if (useBroadcast) {
                initializeBroadcasting();
            }

            if (useMulticast) {
                initializeMulticasting();
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error initializing sockets", e);
        }
    }

    @Override
    protected void onStopProcessing() {
        closeSockets();
    }

    private void initializeBroadcasting() throws UnknownHostException, SocketException {
        InetAddress address = InetAddress.getByAddress(NetworkManager.instance().getWifiBroadcastAddress());
        address = NetworkManager.fastResolveAddress(address);

        broadcastAddress = new InetSocketAddress(address, Constants.PORT_BROADCAST);
        broadcastSocket = new DatagramSocket();
        broadcastSocket.setBroadcast(true);
    }

    private void initializeMulticasting() throws IOException {
        InetAddress groupAddress = InetAddress.getByAddress(Constants.MULTICAST_GROUP_MUSIC_SERVICE);
        groupAddress = NetworkManager.fastResolveAddress(groupAddress);

        multicastSocket = new MulticastSocket();
        multicastSocket.joinGroup(groupAddress);
        multicastAddress = new InetSocketAddress(groupAddress, Constants.PORT_MULTICAST);
    }

    /**
     * Sends the given message to both the broadcast address and multicast
     * address.
     * 
     * @param elem
     * @throws Exception 
     */
    private void sendBroadcastMessage(FrostWireMessage elem, boolean broadcast, boolean multicast) {
        if (!broadcast && !multicast) {
            Log.w(TAG, "A least one of broadcast or multicast should be on to send a message.");
            return;
        }

        byte[] data = elem.toBytes();
        DatagramPacket pack = new DatagramPacket(data, data.length);

        if (broadcast && broadcastSocket != null) {
            pack.setSocketAddress(broadcastAddress);
            try {
                broadcastSocket.send(pack);
            } catch (Throwable e) {
                Log.e(TAG, "Error sending a broadcast to address " + broadcastAddress + ", e=" + e.getMessage());
            }
        }

        if (multicast && multicastSocket != null) {
            pack.setSocketAddress(multicastAddress);
            try {
                multicastSocket.send(pack);
            } catch (Throwable e) {
                Log.e(TAG, "Error sending a multicast to address " + multicastAddress + ", e=" + e.getMessage());
            }
        }
    }

    private void closeSockets() {
        if (broadcastSocket != null) {
            try {
                broadcastSocket.close();
            } catch (Throwable e) {
                Log.e(TAG, "Failed to close socket" + broadcastSocket, e);
            }
        }
        if (multicastSocket != null) {
            try {
                multicastSocket.close();
            } catch (Throwable e) {
                Log.e(TAG, "Failed to close socket" + multicastSocket, e);
            }
        }
    }
}