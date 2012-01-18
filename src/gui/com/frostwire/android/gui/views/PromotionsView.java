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
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.PromotionsHandler;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class PromotionsView extends WebView {

    private static final String TAG = "FW.PromotionsView";

    public PromotionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        try {
            setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.e(TAG, "Failed to load web page: " + failingUrl);
                    loadData("<html/>", "text/html", "utf-8");
                }
            });

            setBackgroundColor(0);

            WebSettings settings = getSettings();
            settings.setJavaScriptEnabled(true);

            addJavascriptInterface(new PromotionsHandler(getContext()), "phi");

            loadUrl(Constants.SERVER_PROMOTIONS_URL);
        } catch (Throwable e) {
            Log.e(TAG, "Error creating view", e);
        }
    }
}
