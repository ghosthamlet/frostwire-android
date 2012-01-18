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

package com.frostwire.android.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.support.v4.util.LruCache;
import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.messages.PingMessage;

/**
 * Keeps track of the Peers we know.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class PeerManager {

    private static final String TAG = "FW.PeerManager";

    private final int maxPeers;
    private final long cacheTimeout;
    private final LruCache<Peer, CacheEntry> peerCache;

    private final PeerComparator peerComparator;

    private Peer localPeer;

    private static PeerManager instance;

    public static PeerManager instance() {
        if (instance == null) {
            instance = new PeerManager();
        }
        return instance;
    }

    private PeerManager() {
        this.maxPeers = Constants.PEER_MANAGER_MAX_PEERS;
        this.cacheTimeout = Constants.PEER_MANAGER_CACHE_TIMEOUT;
        this.peerCache = new LruCache<Peer, CacheEntry>(maxPeers);

        this.peerComparator = new PeerComparator();

        refreshLocalPeer();
    }

    public Peer getLocalPeer() {
        return localPeer;
    }

    public void onMessageReceived(PingMessage elem) {
        Peer peer = new Peer(elem.getAddress(), elem.getListeningPort(), elem);

        if (!peer.isLocalHost()) {
            updatePeerCache(peer, elem.getBye());
        }
    }

    /**
     * This returns a shadow-copy of the peer cache as an ArrayList plus the local peer.
     * 
     * @return
     */
    public List<Peer> getPeers() {
        purgeOld();
        refreshLocalPeer();
        List<Peer> peers = new ArrayList<Peer>(1 + peerCache.size());

        peers.addAll(peerCache.snapshot().keySet());
        Collections.sort(peers, peerComparator);
        peers.add(0, localPeer);

        return peers;
    }

    /**
     * @param uuid
     * @return
     */
    public Peer findPeerByUUID(byte[] uuid) {
        if (uuid == null) {
            return null;
        }

        if (Arrays.equals(uuid, ConfigurationManager.instance().getUUID())) {
            return localPeer;
        }

        Peer k = new Peer();
        k.setUUID(uuid);

        CacheEntry e = peerCache.get(k);

        return e != null ? e.peer : null;
    }

    public void clear() {
        refreshLocalPeer();
        peerCache.evictAll();
    }

    /**
     * Invoke this method whenever you have new information about a peer. For
     * now we invoke this whenever we receive a ping.
     * 
     * @param peer
     * @param disconnected
     */
    private void updatePeerCache(Peer peer, boolean disconnected) {
        // first time we hear from a peer
        if (peerCache.get(peer) == null) {
            // no more ghosts...
            if (disconnected) {
                return;
            }

            // there's no room
            boolean cacheFull = peerCache.size() >= maxPeers;

            // add it to the peer cache
            if (!cacheFull) {
                peerCache.put(peer, new CacheEntry(peer));
                Log.v(TAG, String.format("Adding new peer, total=%s: %s", peerCache.size(), peer));
            }
        } else {
            if (!disconnected) {
                peerCache.put(peer, new CacheEntry(peer)); // touch the element and updates the properties
            } else {
                peerCache.remove(peer);
            }
        }
    }

    private void purgeOld() {
        Map<Peer, CacheEntry> snapshot = peerCache.snapshot();
        // per docs snapshot is a copy of the current contents of the cache,
        // ordered from least recently accessed to most recently accessed.

        long now = System.currentTimeMillis();
        for (CacheEntry entry : snapshot.values()) {
            if (now - entry.timestamp > cacheTimeout) {
                peerCache.remove(entry.peer);
            } else {
                break; // all the elements from this point are fresh
            }
        }
    }

    private void refreshLocalPeer() {
        String nickname = ConfigurationManager.instance().getNickname();
        int numSharedFiles = Librarian.instance().getNumFiles();
        int listeningPort = NetworkManager.instance().getListeningPort();

        PingMessage ping = new PingMessage(listeningPort, numSharedFiles, nickname, false);
        ping.getHeader().setUUID(ConfigurationManager.instance().getUUID());

        localPeer = new Peer(null, listeningPort, ping);
    }

    private static final class CacheEntry {

        public final Peer peer;
        public final long timestamp;

        public CacheEntry(Peer peer) {
            this.peer = peer;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final class PeerComparator implements Comparator<Peer> {
        public int compare(Peer lhs, Peer rhs) {
            int c = lhs.getNickname().compareTo(rhs.getNickname());
            if (c == 0) {
                c = rhs.hashCode() - lhs.hashCode();
            }
            return c;
        }
    }
}