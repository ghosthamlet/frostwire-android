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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
final class ConfigurationDefaults {

    private final Map<String, Object> defaultValues;
    private final Map<String, Object> resetValues;

    public ConfigurationDefaults() {
        defaultValues = new HashMap<String, Object>();
        resetValues = new HashMap<String, Object>();
        load();
    }

    public Map<String, Object> getDefaultValues() {
        return Collections.unmodifiableMap(defaultValues);
    }

    public Map<String, Object> getResetValues() {
        return Collections.unmodifiableMap(resetValues);
    }

    private void load() {
        defaultValues.put(Constants.PREF_KEY_CORE_UUID, convert(UUID.randomUUID()));

        defaultValues.put(Constants.PREF_KEY_GUI_NICKNAME, "FrostNewbie");
        defaultValues.put(Constants.PREF_KEY_GUI_VIBRATE_ON_FINISHED_DOWNLOAD, true);
        defaultValues.put(Constants.PREF_KEY_GUI_SHOW_SHARE_INDICATION, false);
        defaultValues.put(Constants.PREF_KEY_GUI_LAST_MEDIA_TYPE_FILTER, Constants.FILE_TYPE_AUDIO);
        defaultValues.put(Constants.PREF_KEY_GUI_TOS_ACCEPTED, false);
        defaultValues.put(Constants.PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE, false);
        defaultValues.put(Constants.PREF_KEY_GUI_SHOW_TRANSFERS_ON_DOWNLOAD_START, true);
        defaultValues.put(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG, true);

        defaultValues.put(Constants.PREF_KEY_SEARCH_COUNT_DOWNLOAD_FOR_TORRENT_DEEP_SCAN, 5);
        defaultValues.put(Constants.PREF_KEY_SEARCH_COUNT_ROUNDS_FOR_TORRENT_DEEP_SCAN, 3);
        defaultValues.put(Constants.PREF_KEY_SEARCH_INTERVAL_MS_FOR_TORRENT_DEEP_SCAN, 1000);
        defaultValues.put(Constants.PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_DEEP_SCAN, 5);
        defaultValues.put(Constants.PREF_KEY_SEARCH_MAX_TORRENT_FILES_TO_INDEX, 100); // no ultra big torrents here
        defaultValues.put(Constants.PREF_KEY_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT, 256);

        defaultValues.put(Constants.PREF_KEY_SEARCH_USE_BTJUNKIE, true);
        defaultValues.put(Constants.PREF_KEY_SEARCH_USE_CLEARBITS, true);
        defaultValues.put(Constants.PREF_KEY_SEARCH_USE_EXTRATORRENT, true);
        defaultValues.put(Constants.PREF_KEY_SEARCH_USE_ISOHUNT, true);
        defaultValues.put(Constants.PREF_KEY_SEARCH_USE_MININOVA, true);
        defaultValues.put(Constants.PREF_KEY_SEARCH_USE_TPB, true);
        defaultValues.put(Constants.PREF_KEY_SEARCH_USE_VERTOR, true);

        defaultValues.put(Constants.PREF_KEY_NETWORK_USE_RANDOM_LISTENING_PORT, true);
        defaultValues.put(Constants.PREF_KEY_NETWORK_USE_BROADCAST, true);
        defaultValues.put(Constants.PREF_KEY_NETWORK_USE_MULTICAST, true);
        defaultValues.put(Constants.PREF_KEY_NETWORK_USE_MOBILE_DATA, true);
        defaultValues.put(Constants.PREF_KEY_NETWORK_MAX_CONCURRENT_UPLOADS, 3);
        defaultValues.put(Constants.PREF_KEY_NETWORK_PINGS_INTERVAL, 4000);

        defaultValues.put(Constants.PREF_KEY_TRANSFER_SHARE_FINISHED_DOWNLOADS, false);

        defaultValues.put(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS, false);
        defaultValues.put(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY, true);

        defaultValues.put(Constants.PREF_KEY_TORRENT_MAX_DOWNLOAD_SPEED, new Long(0));
        defaultValues.put(Constants.PREF_KEY_TORRENT_MAX_UPLOAD_SPEED, new Long(0));
        defaultValues.put(Constants.PREF_KEY_TORRENT_MAX_DOWNLOADS, new Long(4));
        defaultValues.put(Constants.PREF_KEY_TORRENT_MAX_UPLOADS, new Long(4));
        defaultValues.put(Constants.PREF_KEY_TORRENT_MAX_TOTAL_CONNECTIONS, new Long(250));
        defaultValues.put(Constants.PREF_KEY_TORRENT_MAX_TORRENT_CONNECTIONS, new Long(50));

        resetValue(Constants.PREF_KEY_NETWORK_PINGS_INTERVAL);
    }

    private byte[] convert(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;
    }

    private void resetValue(String key) {
        resetValues.put(key, defaultValues.get(key));
    }
}
