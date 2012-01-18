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

import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.views.AbstractActivity;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class AboutActivity extends AbstractActivity {

    private WebView webView;
    private Button buttonDone;

    public AboutActivity() {
        super(R.layout.activity_about);
    }

    @Override
    protected void initComponents() {
        webView = findView(R.id.activity_about_webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadData("<html/>", "text/html", "utf-8");
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(Constants.SERVER_ABOUT_URL);

        buttonDone = findView(R.id.activity_about_done_button);
        buttonDone.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}