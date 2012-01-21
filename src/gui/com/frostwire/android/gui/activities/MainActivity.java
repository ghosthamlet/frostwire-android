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

import java.io.File;
import java.util.ArrayList;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.SoftwareUpdater;
import com.frostwire.android.gui.fragments.BrowsePeersFragment;
import com.frostwire.android.gui.fragments.SearchFragment;
import com.frostwire.android.gui.fragments.TransfersFragment;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.gui.views.AbstractActivity;
import com.frostwire.android.util.StringUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class MainActivity extends AbstractActivity {

    private static final String TAG = "FW.MainActivity";

    private static final String TAB_SEARCH_KEY = "tab_search";
    private static final String TAB_TRANSFERS_KEY = "tab_transfers";
    private static final String TAB_PEERS_KEY = "tab_peers";

    private TabHost tabHost;
    private ViewPager viewPager;
    private TabsAdapter tabsAdapter;

    public MainActivity() {
        super(R.layout.activity_main, false, 1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            trackDialog(UIUtils.showYesNoDialog(this, R.string.are_you_sure_you_wanna_leave, R.string.minimize_frostwire, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    moveTaskToBack(true);
                }
            }));
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            tabHost.setCurrentTabByTag(TAB_SEARCH_KEY);
        } else {
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabHost = findView(android.R.id.tabhost);
        tabHost.setup();

        viewPager = findView(R.id.pager);

        tabsAdapter = new TabsAdapter(this, tabHost, viewPager);

        View searchIndicator = getLayoutInflater().inflate(R.layout.view_tab_indicator_search, null);
        View transfersIndicator = getLayoutInflater().inflate(R.layout.view_tab_indicator_transfers, null);
        View peersIndicator = getLayoutInflater().inflate(R.layout.view_tab_indicator_peers, null);

        tabsAdapter.addTab(tabHost.newTabSpec(TAB_SEARCH_KEY).setIndicator(searchIndicator), SearchFragment.class, null);
        tabsAdapter.addTab(tabHost.newTabSpec(TAB_TRANSFERS_KEY).setIndicator(transfersIndicator), TransfersFragment.class, null);
        tabsAdapter.addTab(tabHost.newTabSpec(TAB_PEERS_KEY).setIndicator(peersIndicator), BrowsePeersFragment.class, null);

        if (savedInstanceState != null) {
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(Constants.ACTION_ADVICE_UPDATE)) {
            notifyUserToUpdate();
        } else if (action != null && action.equals(Constants.ACTION_SHOW_TRANSFERS)) {
            tabHost.setCurrentTabByTag(TAB_TRANSFERS_KEY);
        }

        if (intent.hasExtra(Constants.EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION)) {
            tabHost.setCurrentTabByTag(TAB_TRANSFERS_KEY);
            TransferManager.instance().clearDownloadsToReview();
            try {
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Constants.NOTIFICATION_DOWNLOAD_TRANSFER_FINISHED);
                Bundle extras = intent.getExtras();
                if (extras.containsKey(Constants.EXTRA_DOWNLOAD_COMPLETE_PATH)) {
                    File file = new File(extras.getString(Constants.EXTRA_DOWNLOAD_COMPLETE_PATH));
                    if (file.isFile()) {
                        UIUtils.openFile(this, file.getAbsoluteFile());
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "Error handling download complete notification", e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Engine.instance().startServices(); // it's necessary for the first time after wizard
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("tab", tabHost.getCurrentTabTag());
    }

    private void notifyUserToUpdate() {
        if (!SystemUtils.getUpdateInstallerPath().exists()) {
            return;
        }

        String message = SoftwareUpdater.instance().getUpdateMessage();
        if (StringUtils.isNullOrEmpty(message, true)) {
            message = getString(R.string.update_message);
        }

        UIUtils.showYesNoDialog(this, R.drawable.application_icon, message, R.string.update_title, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Engine.instance().stopServices(false);
                UIUtils.openFile(MainActivity.this, SystemUtils.getUpdateInstallerPath().getAbsolutePath(), Constants.MIME_TYPE_ANDROID_PACKAGE_ARCHIVE);
            }
        });
    }

    // from an android example:
    // http://developer.android.com/resources/samples/Support4Demos/src/com/example/android/supportv4/app/FragmentTabsPager.html
    private class TabsAdapter extends FragmentPagerAdapter implements OnTabChangeListener, OnPageChangeListener {

        private final Context context;
        private final TabHost tabHost;
        private final ViewPager viewPager;
        private final ArrayList<TabInfo> tabs = new ArrayList<TabInfo>();

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            this.context = activity;
            this.tabHost = tabHost;
            this.viewPager = pager;
            this.tabHost.setOnTabChangedListener(this);
            this.viewPager.setAdapter(this);
            this.viewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clazz, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(context));

            TabInfo info = new TabInfo(clazz, args);
            tabs.add(info);
            tabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = tabs.get(position);
            return Fragment.instantiate(context, info.clazz.getName(), info.args);
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = tabHost.getCurrentTab();
            viewPager.setCurrentItem(position);

            InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(tabHost.getApplicationWindowToken(), 0);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = tabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            tabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        final class TabInfo {
            private final Class<?> clazz;
            private final Bundle args;

            TabInfo(Class<?> clazz, Bundle args) {
                this.clazz = clazz;
                this.args = args;
            }
        }

        class DummyTabFactory implements TabHost.TabContentFactory {

            private final Context context;

            public DummyTabFactory(Context context) {
                this.context = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(context);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }
    }
}
