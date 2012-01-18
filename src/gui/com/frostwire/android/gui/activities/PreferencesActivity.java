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

package com.frostwire.android.gui.activities;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.gui.search.BittorrentSearchEngine;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.gui.views.preference.SimpleActionPreference;
import com.frostwire.android.util.StringUtils;

/**
 * See {@link ConfigurationManager}
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class PreferencesActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.application_preferences);

        setupSeedingOptions();
        setupNickname();
        setupClearIndex();
    }

    private void setupSeedingOptions() {
        final CheckBoxPreference preferenceSeeding = (CheckBoxPreference) findPreference(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS);
        final CheckBoxPreference preferenceSeedingWifiOnly = (CheckBoxPreference) findPreference(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY);

        preferenceSeedingWifiOnly.setEnabled(preferenceSeeding.isChecked());

        preferenceSeeding.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean newVal = (Boolean) newValue;
                if (!newVal) { // not seeding at all
                    TransferManager.instance().stopSeedingTorrents();
                    UIUtils.showShortMessage(PreferencesActivity.this, R.string.seeding_has_been_turned_off);
                }
                preferenceSeedingWifiOnly.setEnabled(newVal);
                return true;
            }
        });

        preferenceSeedingWifiOnly.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean newVal = (Boolean) newValue;
                if (newVal && !NetworkManager.instance().isDataWIFIUp()) { // not seeding on mobile data
                    TransferManager.instance().stopSeedingTorrents();
                    UIUtils.showShortMessage(PreferencesActivity.this, R.string.seeding_has_been_turned_off);
                }
                return true;
            }
        });
    }

    private void setupNickname() {
        EditTextPreference preference = (EditTextPreference) findPreference(Constants.PREF_KEY_GUI_NICKNAME);
        preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newText = ((String) newValue).trim();
                return !StringUtils.isNullOrEmpty(newText, true);
            }
        });
    }

    private void setupClearIndex() {
        final SimpleActionPreference preference = (SimpleActionPreference) findPreference("frostwire.prefs.internal.clear_index");
        updateIndexSummary(preference);
        preference.setOnActionListener(new OnClickListener() {
            public void onClick(View v) {
                int count = BittorrentSearchEngine.clearLocalIndex(PreferencesActivity.this);
                UIUtils.showShortMessage(PreferencesActivity.this, R.string.delete_n_torrents_indexed, count);
                updateIndexSummary(preference);
            }
        });
    }

    private void updateIndexSummary(SimpleActionPreference preference) {
        int count = BittorrentSearchEngine.getLocalIndexCount(this);
        preference.setSummary(getResources().getQuantityString(R.plurals.count_torrents_indexed, count, count));
    }
}