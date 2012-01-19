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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.activities.AboutActivity;
import com.frostwire.android.gui.activities.MainActivity;
import com.frostwire.android.gui.activities.PreferencesActivity;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.UIUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class CommonMenuBuilder implements OptionsMenuBuilder {

    private final static int MENU_PREFERENCES = 0;
    private final static int MENU_TOGGLE_CONNECTION = 1;
    private final static int MENU_ABOUT = 2;

    private final Context context;

    public CommonMenuBuilder(Context context) {
        this.context = context;
    }

    @Override
    public boolean onCreate(Menu menu) {
        menu.add(Menu.NONE, MENU_PREFERENCES, Menu.NONE, R.string.preferences).setIcon(R.drawable.settings);

        if (Engine.instance().isStarted()) {
            menu.add(Menu.NONE, MENU_TOGGLE_CONNECTION, Menu.NONE, R.string.disconnect).setIcon(R.drawable.disconnect);
        } else {
            menu.add(Menu.NONE, MENU_TOGGLE_CONNECTION, Menu.NONE, R.string.connect).setIcon(R.drawable.connect);
        }

        menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.about).setIcon(R.drawable.torrent);

        return true;
    }

    @Override
    public boolean onPrepare(Menu menu) {
        MenuItem item = menu.getItem(MENU_TOGGLE_CONNECTION);

        if (Engine.instance().isStarted()) {
            item.setTitle(R.string.disconnect);
            item.setIcon(R.drawable.disconnect);
            item.setEnabled(true);
        } else if (Engine.instance().isStarting() || Engine.instance().isStopping()) {
            item.setTitle(R.string.im_on_it);
            item.setIcon(R.drawable.clock);
            item.setEnabled(false);
        } else {
            item.setTitle(R.string.connect);
            item.setIcon(R.drawable.connect);
            item.setEnabled(true);
        }

        if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE)) {
            hideMenu(menu, MENU_PREFERENCES);
            hideMenu(menu, MENU_TOGGLE_CONNECTION);
        }

        return true;
    }

    @Override
    public boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_PREFERENCES:
            onMenuPreferences();
            return true;
        case MENU_TOGGLE_CONNECTION:
            onMenuToggleConnection();
            return true;
        case MENU_ABOUT:
            onMenuAbout();
            return true;
        }

        return false;
    }

    private void onMenuPreferences() {
        Intent i = new Intent(context, PreferencesActivity.class);
        context.startActivity(i);
    }

    /**
     * Connect/Disconnect
     * 
     * @param item
     */
    private void onMenuToggleConnection() {
        if (Engine.instance().isStarted()) {

            if (TransferManager.instance().getActiveDownloads() == 0 && TransferManager.instance().getActiveUploads() == 0) {
                disconnect();
                return;
            }

            UIUtils.showYesNoDialog(context, R.string.all_transfers_will_stop_are_you_sure, R.string.leaving_already, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    disconnect();
                }
            });
        } else {
            connect();
        }
    }

    private void onMenuAbout() {
        Intent i = new Intent(context, AboutActivity.class);
        context.startActivity(i);
    }

    private void connect() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Engine.instance().startServices();
                return null;
            }
            
            @Override
            protected void onPostExecute(Void result) {
                UIUtils.showShortMessage(context, R.string.toast_on_connect);
            }
        };
        
        task.execute();
    }

    private void disconnect() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Engine.instance().stopServices(false);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                UIUtils.showShortMessage(context, R.string.toast_on_disconnect);
                if (!(context instanceof MainActivity)) {
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(i);
                }
            }
        };

        task.execute();
    }

    private void hideMenu(Menu menu, int index) {
        MenuItem item = menu.getItem(index);
        item.setEnabled(false);
        item.setVisible(false);
    }
}
