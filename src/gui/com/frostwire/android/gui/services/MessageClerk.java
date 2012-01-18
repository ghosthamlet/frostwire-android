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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.messages.FrostWireMessage;
import com.frostwire.android.core.messages.PingMessage;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * This class is responsible for RECEIVING all the messages sent to the
 * application. It's job is to grab the available datagrams and forward them
 * down as soon as possible to the respective Processor instances.
 * 
 * Before any message can be sent, FrostWire needs to get an instance of the
 * MessageClerk so that it knows
 * 
 * Legend for TrafficClass (TOS) [Although they say that this has never really
 * been supported] 000 (0) - Routine 001 (1) - Priority 010 (2) - Immediate 011
 * (3) - Flash 100 (4) - Flash Override 101 (5) - Critical 110 (6) -
 * Internetwork Control 111 (7) - Network Control
 * 
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class MessageClerk extends QueueProcessor<FrostWireMessage> {

    private static final String TAG = "FW.MessageClerk";

    private final MessageProcessor messageProcessor;

    private SocketClerk<DatagramSocket> broadcastClerk;
    private SocketClerk<MulticastSocket> multicastClerk;

    /**
     * Starts the message clerk instance. This means, preparing the broadcast
     * receiving socket, multicast receiving socket and the generic message
     * socket. And starting worker threads to standby for incoming packets.
     */
    public MessageClerk(ThreadPool pool, MessageProcessor messageProcessor) {
        super("MessageClerk", pool, Constants.MESSAGE_CLERK_CAPACITY);
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void processElement(FrostWireMessage elem) throws Exception {

        MessageProcessor processor = getMessageProcessor(elem.getType());

        if (processor == null) {
            Log.w(TAG, "No message processor exits for type=" + elem.getType());
            return;
        }

        byte type = elem.getType();

        if (type == Constants.MESSAGE_TYPE_PING) {
            PingMessage ping = new PingMessage(elem.getAddress(), elem.toBytes());
            processor.addElement(ping);
            //Log.d(TAG, "PING from " + elem.getOriginAddress());
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

            boolean broadcast = ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_BROADCAST);
            boolean multicast = ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_MULTICAST);

            NetworkManager.instance().lockWifi();
            if (multicast) {
                NetworkManager.instance().lockMulticast();
            }

            if (broadcast) {
                try {
                    initializeBroadcastClerk();
                } catch (Throwable e) {
                    Log.e(TAG, "Unable to start broadcast clerk", e);
                }
            }

            if (multicast) {
                try {
                    initializeMulticastClerk();
                } catch (Throwable e) {
                    Log.e(TAG, "Unable to start multicast clerk", e);
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Unable to start message clerk", e);
        }
    }

    @Override
    protected void onStopProcessing() {
        NetworkManager.instance().unlockMulticast();
        NetworkManager.instance().unlockWifi();

        if (broadcastClerk != null) {
            broadcastClerk.stop();
            broadcastClerk = null;
        }
        if (multicastClerk != null) {
            multicastClerk.stop();
            broadcastClerk = null;
        }
    }

    private void initializeBroadcastClerk() throws IOException {
        DatagramSocket broadcastSocket = new DatagramSocket(null);

        broadcastSocket.setReuseAddress(true);
        broadcastSocket.setBroadcast(true);
        broadcastSocket.setSoTimeout(Constants.DATAGRAM_SOCKET_TIMEOUT);
        broadcastSocket.bind(new InetSocketAddress(Constants.PORT_BROADCAST));

        broadcastClerk = new SocketClerk<DatagramSocket>("BroadcastClerk", broadcastSocket, getThreadPool()) {
            @Override
            protected void onDataReceived(InetAddress origin, byte[] data, int lenght) {
                MessageClerk.this.addReceivedData(origin, data, lenght);
            }

            @Override
            protected boolean isProcessing() {
                return MessageClerk.this.isProcessing();
            }
        };

        broadcastClerk.start();
    }

    private void initializeMulticastClerk() throws IOException {
        InetAddress groupInetAddress = NetworkManager.fastResolveAddress(InetAddress.getByAddress(Constants.MULTICAST_GROUP_MUSIC_SERVICE));

        MulticastSocket multicastSocket = new MulticastSocket(Constants.PORT_MULTICAST);

        multicastSocket.setSoTimeout(Constants.DATAGRAM_SOCKET_TIMEOUT);
        multicastSocket.setReuseAddress(true);
        multicastSocket.setNetworkInterface(NetworkManager.instance().getNetworkInterface());
        multicastSocket.joinGroup(groupInetAddress);

        multicastClerk = new SocketClerk<MulticastSocket>("MulticastClerk", multicastSocket, getThreadPool()) {
            @Override
            protected void onDataReceived(InetAddress origin, byte[] data, int lenght) {
                MessageClerk.this.addReceivedData(origin, data, lenght);
            }

            @Override
            protected boolean isProcessing() {
                return MessageClerk.this.isProcessing();
            }
        };

        multicastClerk.start();
    }

    private void addReceivedData(InetAddress origin, byte[] data, int length) {
        try {
            FrostWireMessage message = new FrostWireMessage(origin, data);

            if (message.getHeader().getVersion() < Constants.FROSTWIRE_PROTOCOL_VERSION) {
                return; // in the future replace this with a filter pattern
            }

            addElement(message);
        } catch (Throwable e) {
            Log.e(TAG, "Error creating message from raw network data, coming from: " + origin);
        }
    }

    private MessageProcessor getMessageProcessor(byte messageType) {
        return messageProcessor;
    }
}
