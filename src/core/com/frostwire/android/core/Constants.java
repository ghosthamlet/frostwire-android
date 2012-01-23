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

package com.frostwire.android.core;

/**
 * Static class containing all constants in one place.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class Constants {

    private Constants() {
    }

    public static final byte[] FROSTWIRE_VERSION = { (byte) 0, (byte) 9, (byte) 4 };

    public static final byte[] CLIENT_IDENTIFIER = { (byte) 0x46, (byte) 0x57 };

    public static final String FROSTWIRE_VERSION_STRING = FROSTWIRE_VERSION[0] + "." + FROSTWIRE_VERSION[1] + "." + FROSTWIRE_VERSION[2];

    /**
     * Self announcing message. To be sent via Multicast or Broadcast
     */
    public static final byte MESSAGE_TYPE_PING = (byte) 0;

    /**
     * First 2 bytes of every FrostWire/Android Message header -> "FW"
     */
    public static final byte[] MESSAGE_HEADER_FIELD_FW = { (byte) 0x46, (byte) 0x57 };

    public static final byte FROSTWIRE_PROTOCOL_VERSION_3 = (byte) 0x03;
    public static final byte FROSTWIRE_PROTOCOL_VERSION_4 = (byte) 0x04;
    public static final byte FROSTWIRE_PROTOCOL_VERSION_5 = (byte) 0x05;

    public static final byte FROSTWIRE_PROTOCOL_VERSION = FROSTWIRE_PROTOCOL_VERSION_5;

    public static final byte[] MULTICAST_GROUP_MUSIC_SERVICE = new byte[] { (byte) 224, 0, 1, 16 };

    /**
     * 65280 - Default LAN bound port
     */
    public static final int GENERIC_LISTENING_PORT = 0xff00;

    public static final int PORT_MULTICAST = 0xffa0; // 65440
    public static final int PORT_BROADCAST = 0xffb0; // 65456

    public static final int FW_HEADER_SIZE = 30;

    // preference keys
    public static final String PREF_KEY_CORE_UUID = "frostwire.prefs.core.uuid";

    public static final String PREF_KEY_NETWORK_USE_BROADCAST = "froswire.prefs.network.use_broadcast";
    public static final String PREF_KEY_NETWORK_USE_MULTICAST = "froswire.prefs.network.use_multicast";
    public static final String PREF_KEY_NETWORK_USE_MOBILE_DATA = "frostwire.prefs.network.use_mobile_data";
    public static final String PREF_KEY_NETWORK_USE_RANDOM_LISTENING_PORT = "frostwire.prefs.network.use_random_listening_port";
    public static final String PREF_KEY_NETWORK_MAX_CONCURRENT_UPLOADS = "frostwire.prefs.network.max_concurrent_uploads";
    public static final String PREF_KEY_NETWORK_PINGS_INTERVAL = "frostwire.prefs.network.pings_interval";

    public static final String PREF_KEY_TRANSFER_SHARE_FINISHED_DOWNLOADS = "frostwire.prefs.transfer.share_finished_downloads";

    public static final String PREF_KEY_SEARCH_COUNT_DOWNLOAD_FOR_TORRENT_DEEP_SCAN = "frostwire.prefs.search.count_download_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_COUNT_ROUNDS_FOR_TORRENT_DEEP_SCAN = "frostwire.prefs.search.count_rounds_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_INTERVAL_MS_FOR_TORRENT_DEEP_SCAN = "frostwire.prefs.search.interval_ms_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_DEEP_SCAN = "frostwire.prefs.search.min_seeds_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_MAX_TORRENT_FILES_TO_INDEX = "frostwire.prefs.search.max_torrent_files_to_index";
    public static final String PREF_KEY_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT = "frostwire.prefs.search.fulltext_search_results_limit";

    public static final String PREF_KEY_SEARCH_USE_BTJUNKIE = "frostwire.prefs.search.use_btjunkie";
    public static final String PREF_KEY_SEARCH_USE_CLEARBITS = "frostwire.prefs.search.use_clearbits";
    public static final String PREF_KEY_SEARCH_USE_EXTRATORRENT = "frostwire.prefs.search.use_extratorrent";
    public static final String PREF_KEY_SEARCH_USE_ISOHUNT = "frostwire.prefs.search.use_isohunt";
    public static final String PREF_KEY_SEARCH_USE_MININOVA = "frostwire.prefs.search.use_mininova";
    public static final String PREF_KEY_SEARCH_USE_TPB = "frostwire.prefs.search.use_tpb";
    public static final String PREF_KEY_SEARCH_USE_VERTOR = "frostwire.prefs.search.use_vertor";

    public static final String PREF_KEY_SEARCH_PREFERENCE_CATEGORY = "frostwire.prefs.search.preference_category";

    public static final String PREF_KEY_GUI_NICKNAME = "frostwire.prefs.gui.nickname";
    public static final String PREF_KEY_GUI_VIBRATE_ON_FINISHED_DOWNLOAD = "frostwire.prefs.gui.vibrate_on_finished_download";
    public static final String PREF_KEY_GUI_SHOW_SHARE_INDICATION = "frostwire.prefs.gui.show_share_indication";
    public static final String PREF_KEY_GUI_LAST_MEDIA_TYPE_FILTER = "frostwire.prefs.gui.last_media_type_filter";
    public static final String PREF_KEY_GUI_TOS_ACCEPTED = "frostwire.prefs.gui.tos_accepted";
    public static final String PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE = "frostwire.prefs.gui.initial_settings_complete";
    public static final String PREF_KEY_GUI_SHOW_TRANSFERS_ON_DOWNLOAD_START = "frostwire.prefs.gui.show_transfers_on_download_start";
    public static final String PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG = "frostwire.prefs.gui.show_new_transfer_dialog";

    public static final String PREF_KEY_TORRENT_MAX_DOWNLOAD_SPEED = "frostwire.prefs.torrent.max_download_speed";
    public static final String PREF_KEY_TORRENT_MAX_UPLOAD_SPEED = "frostwire.prefs.torrent.max_upload_speed";
    public static final String PREF_KEY_TORRENT_MAX_DOWNLOADS = "frostwire.prefs.torrent.max_downloads";
    public static final String PREF_KEY_TORRENT_MAX_UPLOADS = "frostwire.prefs.torrent.max_uploads";
    public static final String PREF_KEY_TORRENT_MAX_TOTAL_CONNECTIONS = "frostwire.prefs.torrent.max_total_connections";
    public static final String PREF_KEY_TORRENT_MAX_TORRENT_CONNECTIONS = "frostwire.prefs.torrent.max_torrent_connections";
    public static final String PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS = "frostwire.prefs.torrent.seed_finished_torrents";
    public static final String PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY = "frostwire.prefs.torrent.seed_finished_torrents_wifi_only";

    public static final String ACTION_OPEN_TORRENT_URL = "android.intent.action.VIEW";
    public static final String ACTION_SHOW_TRANSFERS = "com.frostwire.android.ACTION_SHOW_TRANSFERS";
    public static final String ACTION_MEDIA_PLAYER_STOPPED = "com.frostwire.android.ACTION_MEDIA_PLAYER_STOPPED";
    public static final String ACTION_REFRESH_FINGER = "com.frostwire.android.ACTION_REFRESH_FINGER";
    public static final String EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION = "com.frostwire.android.EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION";
    public static final String EXTRA_DOWNLOAD_COMPLETE_PATH = "com.frostwire.android.EXTRA_DOWNLOAD_COMPLETE_PATH";
    public static final String EXTRA_PEER_UUID = "com.frostwire.android.EXTRA_PEER_UUID";

    public static final int NOTIFICATION_MEDIA_PLAYING_ID = 1000;
    public static final int NOTIFICATION_DOWNLOAD_TRANSFER_FINISHED = 1001;

    // generic file types
    public static final byte FILE_TYPE_AUDIO = 0x00;
    public static final byte FILE_TYPE_PICTURES = 0x01;
    public static final byte FILE_TYPE_VIDEOS = 0x02;
    public static final byte FILE_TYPE_DOCUMENTS = 0x03;
    public static final byte FILE_TYPE_APPLICATIONS = 0x04;
    public static final byte FILE_TYPE_RINGTONES = 0x05;
    public static final byte FILE_TYPE_TORRENTS = 0x06;

    public static final String MIME_TYPE_ANDROID_PACKAGE_ARCHIVE = "application/vnd.android.package-archive";

    /**
     * URL where FrostWIre checks for software updates
     */
    public static final String SERVER_UPDATE_URL = "http://update.frostwire.com/android";

    public static final String SERVER_PROMOTIONS_URL = "http://update.frostwire.com/android.promos.php";

    public static final String SERVER_ABOUT_URL = "http://update.frostwire.com/android.about.php";

    /**
     * Maximum number of peers the PeerManager should hold
     */
    public static final int PEER_MANAGER_MAX_PEERS = 15;

    public static final int PEER_MANAGER_CACHE_TIMEOUT = 5000;

    public static final int DATAGRAM_SOCKET_TIMEOUT = 60000; //500;

    public static final long LIBRARIAN_FILE_COUNT_CACHE_TIMEOUT = 2 * 60 * 1000; // 2 minutes

    public static final int MESSAGE_PROCESSOR_CAPACITY = 50;

    public static final int MESSAGE_CLERK_CAPACITY = 50;

    public static final int MESSAGE_COURIER_CAPACITY = 50;

    public static final int MAX_NUM_DOWNLOAD_CHECKED = 5;

    public static final int MAX_INDEXED_TORRENT_SUB_FILES = 4000;

    public static final int MAX_PEER_HTTP_DOWNLOAD_RETRIES = 3;
}