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

package com.frostwire.android.gui.search;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONArray;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

import com.frostwire.android.R;
import com.frostwire.android.core.HttpFetcher;
import com.frostwire.android.util.StringUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class SuggestionsAdapter extends SimpleCursorAdapter {

    private static final String SUGGESTIONS_URL = buildSuggestionsUrl();
    private static final int HTTP_QUERY_TIMEOUT = 1000;

    private boolean discardLastResult;

    public SuggestionsAdapter(Context context) {
        super(context, R.layout.view_suggestion_item, null, new String[] { SuggestionsCursor.COLUMN_SUGGESTION }, new int[] { R.id.view_suggestion_item_text }, 0);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        try {
            String url = String.format(SUGGESTIONS_URL, URLEncoder.encode(constraint.toString(), "UTF-8"));

            HttpFetcher fetcher = new HttpFetcher(new URI(url), HTTP_QUERY_TIMEOUT);
            String json = new String(fetcher.fetch());

            if (!discardLastResult) {
                return new SuggestionsCursor(new JSONArray(json).getJSONArray(1));
            }
        } catch (Throwable e) {
            // ignore
        } finally {
            discardLastResult = false;
        }

        return null;
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        if (cursor != null) {
            return cursor.getString(1);
        }
        return null;
    }

    public void discardLastResult() {
        discardLastResult = true;
    }

    private static String buildSuggestionsUrl() {
        String lang = Locale.getDefault().getLanguage();
        if (StringUtils.isNullOrEmpty(lang)) {
            lang = "en";
        }

        return "http://suggestqueries.google.com/complete/search?output=firefox&hl=" + Locale.getDefault() + "&q=%s";
    }
}