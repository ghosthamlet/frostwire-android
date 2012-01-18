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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ShareIndicationDialog extends Dialog {

    private Button buttonDone;
    private CheckBox checkShow;

    public ShareIndicationDialog(Context context) {
        super(context);
        initComponents();
    }

    @Override
    public void show() {
        show(false);
    }

    public Dialog show(boolean force) {
        if (force || getPreferences().getBoolean(Constants.PREF_KEY_GUI_SHOW_SHARE_INDICATION, true)) {
            super.show();
        }
        return this;
    }

    private void initComponents() {
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

        setTitle(R.string.share_indication_dialog_title);
        setContentView(R.layout.dialog_share_indication);
        setCancelable(true);

        buttonDone = (Button) findViewById(R.id.dialog_share_indicator_button_done);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                getPreferences().edit().putBoolean(Constants.PREF_KEY_GUI_SHOW_SHARE_INDICATION, checkShow.isChecked()).commit();
            }
        });

        checkShow = (CheckBox) findViewById(R.id.dialog_share_indicator_check_show);
        checkShow.setChecked(getPreferences().getBoolean(Constants.PREF_KEY_GUI_SHOW_SHARE_INDICATION, true));
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }
}
