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

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

/**
 * 
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public abstract class AbstractActivity extends FragmentActivity {

    private static final String TAG = "FW.AbstractActivity";

    private final int layoutResId;
    private final boolean title;
    private final OptionsMenuBuilder optionsMenuBuilder;

    private final List<Dialog> dialogs;

    private final long refreshInterval;
    private final List<Refreshable> refreshables;

    private Handler refreshHandler;
    private Runnable refreshTask;

    public AbstractActivity(int layoutResId, boolean title, int refreshIntervalSec) {
        this.layoutResId = layoutResId;
        this.title = title;
        this.optionsMenuBuilder = getOptionsMenuBuilder();

        this.dialogs = new ArrayList<Dialog>();

        this.refreshInterval = refreshIntervalSec * 1000;
        this.refreshables = new ArrayList<Refreshable>();

        if (refreshInterval > 0) {
            refreshHandler = new Handler();
            refreshTask = new Runnable() {
                public void run() {
                    onRefresh();
                    refreshHandler.postDelayed(refreshTask, refreshInterval);
                }
            };
        }
    }

    public AbstractActivity(int layoutResID) {
        this(layoutResID, true, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (optionsMenuBuilder != null) {
            return optionsMenuBuilder.onCreate(menu);
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (optionsMenuBuilder != null) {
            return optionsMenuBuilder.onPrepare(menu);
        } else {
            return super.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (optionsMenuBuilder != null) {
            return optionsMenuBuilder.onItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void addRefreshable(Refreshable refreshable) {
        if (!refreshables.contains(refreshable)) {
            refreshables.add(refreshable);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        if (!title) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        if (layoutResId != 0) {
            setContentView(layoutResId);
            initComponents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (refreshHandler != null) {
            refreshHandler.postDelayed(refreshTask, refreshInterval);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshTask);
        }

        dismissDialogs();
    }

    protected void onRefresh() {
        for (Refreshable refreshable : refreshables) {
            try {
                refreshable.refresh();
            } catch (Throwable e) {
                Log.v(TAG, "Error refreshing component", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected final <T extends View> T findView(int id) {
        return (T) super.findViewById(id);
    }

    protected void initComponents() {
    }

    protected OptionsMenuBuilder getOptionsMenuBuilder() {
        return new CommonMenuBuilder(this);
    }

    protected Dialog trackDialog(Dialog dialog) {
        if (!dialogs.contains(dialog)) {
            dialogs.add(dialog);
        }
        return dialog;
    }

    private void dismissDialogs() {
        for (Dialog dialog : dialogs) {
            try {
                dialog.dismiss();
            } catch (Throwable e) {
                Log.w(TAG, "Error dismissing dialog", e);
            }
        }
    }
}
