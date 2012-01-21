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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.MediaType;
import com.frostwire.android.gui.search.SuggestionsAdapter;
import com.frostwire.android.gui.views.ClearableEditTextView.OnActionListener;
import com.frostwire.android.gui.views.QuickAction.OnActionItemClickListener;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchInputView extends LinearLayout {

    private static final String TAG = "FW.SearchInputView";
    
    private final SuggestionsAdapter adapter;

    private Button buttonOptions;
    private ClearableEditTextView textInput;
    private Button buttonSearch;

    private QuickAction quickAction;

    private OnSearchListener onSearchListener;

    private int mediaTypeId;

    public SearchInputView(Context context, AttributeSet set) {
        super(context, set);
        
        this.adapter = new SuggestionsAdapter(context);
    }

    public OnSearchListener getOnSearchListener() {
        return onSearchListener;
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
    }

    public boolean isEmpty() {
        return textInput.getText().length() == 0;
    }

    public void hideQuickAction() {
        quickAction.dismiss();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_search_input, this);

        try {
            setupQuickAction();

            textInput = (ClearableEditTextView) findViewById(R.id.view_search_text_input);
            textInput.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                        startSearch(v);
                        return true;
                    }
                    return false;
                }
            });
            textInput.setOnActionListener(new OnActionListener() {
                public void onTextChanged(View v, String str) {
                }

                public void onClear(View v) {
                    SearchInputView.this.onClear();
                }
            });
            textInput.setAdapter(adapter);

            buttonSearch = (Button) findViewById(R.id.view_search_button_search);
            buttonSearch.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    startSearch(v);
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "Error creating view", e);
        }
    }

    private void setupQuickAction() {
        Resources rs = getResources();
        QuickActionItem audio = new QuickActionItem(MediaType.getAudioMediaType().getId(), rs.getString(R.string.media_type_audio), rs.getDrawable(R.drawable.audio));
        QuickActionItem image = new QuickActionItem(MediaType.getImageMediaType().getId(), rs.getString(R.string.media_type_images), rs.getDrawable(R.drawable.picture));
        QuickActionItem video = new QuickActionItem(MediaType.getVideoMediaType().getId(), rs.getString(R.string.media_type_video), rs.getDrawable(R.drawable.video));
        QuickActionItem document = new QuickActionItem(MediaType.getDocumentMediaType().getId(), rs.getString(R.string.media_type_documents), rs.getDrawable(R.drawable.document));
        QuickActionItem application = new QuickActionItem(MediaType.getApplicationsMediaType().getId(), rs.getString(R.string.media_type_applications), rs.getDrawable(R.drawable.application));
        QuickActionItem torrent = new QuickActionItem(MediaType.getTorrentMediaType().getId(), rs.getString(R.string.media_type_torrents), rs.getDrawable(R.drawable.torrent));
        
        quickAction = new QuickAction(getContext(), isLandscape() ? QuickAction.HORIZONTAL : QuickAction.VERTICAL);

        // add action items into QuickAction
        quickAction.addActionItem(audio);
        quickAction.addActionItem(image);
        quickAction.addActionItem(video);
        quickAction.addActionItem(document);
        quickAction.addActionItem(application);
        quickAction.addActionItem(torrent);

        quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {
            public void onItemClick(QuickAction source, int pos, int actionId) {
                onMediaTypeSelected(actionId);

                QuickActionItem item = source.getActionItem(pos);
                buttonOptions.setBackgroundDrawable(item.getIcon());
                mediaTypeId = actionId;
                ConfigurationManager.instance().setLastMediaTypeFilter(mediaTypeId);
            }
        });
        quickAction.setBackgroundAlpha(235);

        mediaTypeId = ConfigurationManager.instance().getLastMediaTypeFilter();

        buttonOptions = (Button) findViewById(R.id.view_search_button_options);
        buttonOptions.setBackgroundResource(getDrawableId(mediaTypeId));
        buttonOptions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction.show(v);
            }
        });
    }

    private int getDrawableId(int mediaTypeId) {
        
        if (MediaType.getApplicationsMediaType().getId() == mediaTypeId) {
            return R.drawable.application;
        } else if (MediaType.getAudioMediaType().getId() == mediaTypeId) {
            return R.drawable.audio;
        } else if (MediaType.getDocumentMediaType().getId() == mediaTypeId) {
            return R.drawable.document;
        } else if (MediaType.getImageMediaType().getId() == mediaTypeId) {
            return R.drawable.picture;
        } else if (MediaType.getVideoMediaType().getId() == mediaTypeId) {
            return R.drawable.video;
        } else if (MediaType.getTorrentMediaType().getId() == mediaTypeId) {
            return R.drawable.torrent;
        } else {
            return R.drawable.question_mark;
        }
    }

    private void startSearch(View v) {
        hideSoftInput(v);
        textInput.setListSelection(-1);
        textInput.dismissDropDown();
        adapter.discardLastResult();

        String query = textInput.getText().toString().trim();
        if (query.length() > 0) {
            onSearch(query, mediaTypeId);
        }
    }

    private void onSearch(String query, int mediaTypeId) {
        if (onSearchListener != null) {
            onSearchListener.onSearch(this, query, mediaTypeId);
        }
    }

    private void onMediaTypeSelected(int mediaTypeId) {
        if (onSearchListener != null) {
            onSearchListener.onMediaTypeSelected(this, mediaTypeId);
        }
    }

    private void onClear() {
        if (onSearchListener != null) {
            onSearchListener.onClear(this);
        }
    }

    private void hideSoftInput(View v) {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static interface OnSearchListener {

        public void onSearch(View v, String query, int mediaTypeId);

        public void onMediaTypeSelected(View v, int mediaTypeId);

        public void onClear(View v);
    }
}