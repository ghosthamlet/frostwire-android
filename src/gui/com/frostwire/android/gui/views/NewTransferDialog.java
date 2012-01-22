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
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.search.SearchResult;
import com.frostwire.android.gui.util.UIUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class NewTransferDialog extends Dialog {

    private Button buttonNo;
    private Button buttonYes;
    private CheckBox checkShow;

    private final OnYesNoListener listener;
    private final SearchResult searchResult;
    
    /** When opening .torrent files from outside you don't want to
     * give the user the option of not showing this dialog again,
     * and the question should be asked everytime to avoid starting
     * big transfers by mistake. */
    private boolean hideShowNextTimeOption;

    public NewTransferDialog(Context context, SearchResult searchResult, boolean hideShowNextTimeOption,  OnYesNoListener listener) {
        super(context);
        this.listener = listener;
        this.searchResult = searchResult;
        this.hideShowNextTimeOption = hideShowNextTimeOption;
        initComponents();
    }

    @Override
    public void show() {
        if (hideShowNextTimeOption || getPreferences().getBoolean(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG, true)) {
            super.show();
        } else {
            if (listener != null) {
                listener.onYes(this);
            }
        }
    }

    private void initComponents() {
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

        setTitle(R.string.dialog_new_transfer_title);
        setContentView(R.layout.dialog_new_transfer);
        
        TextView textQuestion = (TextView) findViewById(R.id.dialog_new_transfer_text);

        String sizeString = getContext().getString(R.string.size_unknown);
        if (searchResult.getSize() > 0) {
            sizeString = UIUtils.getBytesInHuman(searchResult.getSize());
        }
        
        textQuestion.setText(getContext().getString(R.string.dialog_new_transfer_text_text,searchResult.getTitle(),sizeString));
        
        setCancelable(true);

        buttonNo = (Button) findViewById(R.id.dialog_new_transfer_button_no);
        buttonNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onNo(NewTransferDialog.this);
                }
            }
        });

        buttonYes = (Button) findViewById(R.id.dialog_new_transfer_button_yes);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onYes(NewTransferDialog.this);
                }
            }
        });

        checkShow = (CheckBox) findViewById(R.id.dialog_new_transfer_check_show);
        checkShow.setChecked(getPreferences().getBoolean(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG, true));
        checkShow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getPreferences().edit().putBoolean(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG, checkShow.isChecked()).commit();
            }
        });
        
        if (hideShowNextTimeOption) {
            checkShow.setVisibility(View.GONE);
        }
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public interface OnYesNoListener {

        public void onYes(NewTransferDialog dialog);

        public void onNo(NewTransferDialog dialog);
    }
}
