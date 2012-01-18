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

package com.frostwire.android.gui.views;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.util.StringUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class GeneralWizardPage extends RelativeLayout implements WizardPageView {

    private OnCompleteListener listener;

    private EditText inputNickname;
    private CheckBox checkSeedFinishedTorrents;
    private CheckBox checkSeedFinishedTorrentsWifiOnly;

    public GeneralWizardPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public void load() {
        inputNickname.setText(ConfigurationManager.instance().getNickname());
        checkSeedFinishedTorrents.setChecked(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS));
        checkSeedFinishedTorrentsWifiOnly.setChecked(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY));
        checkSeedFinishedTorrentsWifiOnly.setEnabled(checkSeedFinishedTorrents.isChecked());
        checkSeedFinishedTorrentsWifiOnly.setTextColor((checkSeedFinishedTorrents.isChecked()) ? Color.WHITE : getContext().getResources().getColor(R.color.frostwire_gray_explanation_text) );
        validate();
    }

    @Override
    public void finish() {
        ConfigurationManager.instance().setNickname(inputNickname.getText().toString());
        ConfigurationManager.instance().setBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS, checkSeedFinishedTorrents.isChecked());
        ConfigurationManager.instance().setBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY, checkSeedFinishedTorrentsWifiOnly.isChecked());
    }

    @Override
    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_general_wizard_page, this);

        inputNickname = (EditText) findViewById(R.id.view_general_wizard_page_input_nickname);
        inputNickname.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        checkSeedFinishedTorrents = (CheckBox) findViewById(R.id.view_general_wizard_page_check_seed_finished_torrents);
        checkSeedFinishedTorrents.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkSeedFinishedTorrentsWifiOnly.setEnabled(isChecked);
                checkSeedFinishedTorrentsWifiOnly.setTextColor((isChecked) ? Color.WHITE : getContext().getResources().getColor(R.color.frostwire_gray_explanation_text) );
                validate();
            }
        });

        checkSeedFinishedTorrentsWifiOnly = (CheckBox) findViewById(R.id.view_general_wizard_page_check_seed_finished_torrents_wifi_only);
        checkSeedFinishedTorrentsWifiOnly.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validate();
            }
        });
    }

    protected void onComplete(boolean complete) {
        if (listener != null) {
            listener.onComplete(this, complete);
        }
    }

    /**
     * Put more complete/validation logic here.
     */
    private void validate() {
        boolean complete = true;

        if (StringUtils.isNullOrEmpty(inputNickname.getText().toString(), true)) {
            complete = false;
        }

        onComplete(complete);
    }
}
