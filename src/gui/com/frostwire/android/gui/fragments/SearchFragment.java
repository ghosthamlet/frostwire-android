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

package com.frostwire.android.gui.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.gui.adapters.SearchResultListAdapter;
import com.frostwire.android.gui.search.BittorrentSearchEngine;
import com.frostwire.android.gui.search.BittorrentSearchResult;
import com.frostwire.android.gui.search.SearchResult;
import com.frostwire.android.gui.search.SearchResultDisplayer;
import com.frostwire.android.gui.transfers.DownloadTransfer;
import com.frostwire.android.gui.views.AbstractListFragment;
import com.frostwire.android.gui.views.SearchInputView;
import com.frostwire.android.gui.views.SearchInputView.OnSearchListener;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchFragment extends AbstractListFragment implements
        SearchResultDisplayer {

    private static final int MAX_EXPECT_COUNT = 5; // tuning constants for hide progress dialog

    private SearchInputView searchInput;

    private Object lockObj = new Object();

    private SearchResultListAdapter adapter;
    private BittorrentSearchEngine searchManager;

    private int mediaTypeId;

    private int expectCount;
    private ProgressDialog progressDlg;

    public SearchFragment() {
        super(R.layout.fragment_search);
        mediaTypeId = ConfigurationManager.instance().getLastMediaTypeFilter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
    }

    public void clear() {
        synchronized (lockObj) {
            if (adapter != null) {
                adapter.clear();
            }
        }
    }

    public void addResults(final List<SearchResult> results) {
        synchronized (lockObj) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (!searchInput.isEmpty()) {
                        if (adapter == null) {
                            adapter = new SearchResultListAdapter(
                                    getActivity(), results) {
                                @Override
                                protected void onTransferStarted(
                                        DownloadTransfer transfer) {
                                    searchManager.cancelSearch();
                                }
                            };
                            adapter.filter(mediaTypeId);
                            setListAdapter(adapter);
                        } else {
                            List<SearchResult> list = adapter.getList();
                            list.addAll(results); // heavy use of OO references
                            adapter.sort(new Comparator<SearchResult>() {
                                public int compare(SearchResult lhs,
                                        SearchResult rhs) {
                                    if (lhs instanceof BittorrentSearchResult) {
                                        if (rhs instanceof BittorrentSearchResult) {
                                            return ((BittorrentSearchResult) rhs)
                                                    .getSeeds()
                                                    - ((BittorrentSearchResult) lhs)
                                                            .getSeeds();
                                        } else {
                                            return -1;
                                        }
                                    }
                                    return 0;
                                }
                            });
                            adapter.filter(mediaTypeId);
                        }
                    }

                    if (adapter != null) {
                        if (adapter.getCount() > 0) {
                            hideProgressDialog();
                        } else {
                            if (expectCount > MAX_EXPECT_COUNT) {
                                hideProgressDialog();
                            } else {
                                expectCount++;
                            }
                        }
                    }
                }
            });
        }
    }

    public void addResult(final SearchResult sr) {
        synchronized (lockObj) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    hideProgressDialog();
                    if (!searchInput.isEmpty() && adapter != null
                            && sr instanceof BittorrentSearchResult) {
                        BittorrentSearchResult bsr = (BittorrentSearchResult) sr;
                        adapter.addItem(sr, adapter.accept(bsr, mediaTypeId));
                    }
                }
            });
        }
    }

    public List<SearchResult> getResults() {
        synchronized (lockObj) {
            if (adapter != null) {
                return new ArrayList<SearchResult>(adapter.getList());
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    protected void initComponents(final View view) {
        searchInput = findView(view, R.id.fragment_search_input);
        searchInput.setOnSearchListener(new OnSearchListener() {
            public void onSearch(View v, String query, int mediaTypeId) {
                SearchFragment.this.mediaTypeId = mediaTypeId;
                switchView(view,android.R.id.list);
                showProgressDialog();
                searchManager.performSearch(query);
            }

            public void onMediaTypeSelected(View v, int mediaTypeId) {
                SearchFragment.this.mediaTypeId = mediaTypeId;
                if (adapter != null) {
                    adapter.filter(mediaTypeId);
                }
            }

            public void onClear(View v) {
                switchView(view,R.id.fragment_search_promos);
                searchManager.cancelSearch();
                adapter = null;
                setListAdapter(null);
            }
        });

        searchManager = new BittorrentSearchEngine(getActivity(), this);
        
        switchView(view,R.id.fragment_search_promos);
    }

    @Override
    protected void dismissDialogs() {
        super.dismissDialogs();

        searchInput.hideQuickAction();

        if (adapter != null) {
            adapter.dismissDialogs();
        }
    }

    private void showProgressDialog() {
        hideProgressDialog();

        expectCount = 0;

        progressDlg = new ProgressDialog(getActivity());
        progressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDlg.setMessage(getString(R.string.searching_indeterminate));
        progressDlg.setCancelable(false);
        trackDialog(progressDlg).show();
    }

    private void hideProgressDialog() {
        if (progressDlg != null) {
            try {
                progressDlg.dismiss();
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    private void switchView(View v, int id) {
        FrameLayout frameLayout = findView(v,
                R.id.fragment_search_framelayout);

        int childCount = frameLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = frameLayout.getChildAt(i);
            childAt.setVisibility((childAt.getId() == id) ? View.VISIBLE
                    : View.INVISIBLE);
        }
    }
}