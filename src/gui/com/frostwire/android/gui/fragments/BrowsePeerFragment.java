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

import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.gui.Finger;
import com.frostwire.android.gui.Peer;
import com.frostwire.android.gui.PeerManager;
import com.frostwire.android.gui.adapters.FileListAdapter;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.gui.views.AbstractListFragment;
import com.frostwire.android.gui.views.BrowsePeerSearchBarView;
import com.frostwire.android.gui.views.BrowsePeerSearchBarView.OnActionListener;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class BrowsePeerFragment extends AbstractListFragment implements LoaderCallbacks<Object> {

    private static final String TAG = "FW.BrowsePeerFragment";

    private static final int LOADER_FINGER_ID = 0;
    private static final int LOADER_FILES_ID = 1;

    private final BroadcastReceiver broadcastReceiver;

    private RadioButton buttonAudio;
    private RadioButton buttonPictures;
    private RadioButton buttonVideos;
    private RadioButton buttonRingtones;
    private RadioButton buttonApplications;
    private RadioButton buttonDocuments;

    private BrowsePeerSearchBarView filesBar;

    private FileListAdapter adapter;

    private Peer peer;
    private boolean local;
    private Finger finger;

    private ProgressDialog progressDlg;

    public BrowsePeerFragment() {
        super(R.layout.fragment_browse_peer);

        broadcastReceiver = new LocalBroadcastReceiver();
    }

    public Peer getPeer() {
        if (peer == null) {
            loadPeerFromIntentData();
        }
        return peer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (peer == null) {
            loadPeerFromIntentData();
        }

        if (peer == null) { // save move
            getActivity().finish();
            return;
        }

        setRetainInstance(true);

        getLoaderManager().restartLoader(LOADER_FINGER_ID, null, this);
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        showProgressDialog();

        if (id == LOADER_FINGER_ID) {
            return createLoaderFinger();
        } else if (id == LOADER_FILES_ID) {
            return createLoaderFiles(args.getByte("fileType"));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        hideProgressDialog();

        if (data == null) {
            Log.w(TAG, "Something wrong, data is null");
            UIUtils.showShortMessage(getActivity(), R.string.is_not_responding, peer.getNickname());
            getActivity().finish();
            return;
        }

        if (loader.getId() == LOADER_FINGER_ID) {
            boolean checkAudio = finger == null;
            finger = (Finger) data;
            updateHeader();
            if (checkAudio) {
                buttonAudio.setChecked(true);
            }
        } else if (loader.getId() == LOADER_FILES_ID) {
            updateFiles((Object[]) data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTION_MEDIA_PLAYER_STOPPED));
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTION_REFRESH_FINGER));
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void dismissDialogs() {
        super.dismissDialogs();

        if (adapter != null) {
            adapter.dismissDialogs();
        }
    }

    @Override
    protected void initComponents(View v) {
        buttonApplications = initRadioButton(v, R.id.fragment_browse_peer_radio_applications, Constants.FILE_TYPE_APPLICATIONS);
        buttonDocuments = initRadioButton(v, R.id.fragment_browse_peer_radio_documents, Constants.FILE_TYPE_DOCUMENTS);
        buttonPictures = initRadioButton(v, R.id.fragment_browse_peer_radio_pictures, Constants.FILE_TYPE_PICTURES);
        buttonVideos = initRadioButton(v, R.id.fragment_browse_peer_radio_videos, Constants.FILE_TYPE_VIDEOS);
        buttonRingtones = initRadioButton(v, R.id.fragment_browse_peer_radio_ringtones, Constants.FILE_TYPE_RINGTONES);
        buttonAudio = initRadioButton(v, R.id.fragment_browse_peer_radio_audio, Constants.FILE_TYPE_AUDIO);

        filesBar = findView(v, R.id.fragment_browse_peer_files_bar);
        filesBar.setOnActionListener(new OnActionListener() {
            public void onCheckAll(View v, boolean isChecked) {
                if (adapter != null) {
                    if (isChecked) {
                        adapter.checkAll();
                    } else {
                        adapter.clearChecked();
                    }
                }
            }

            public void onFilter(View v, String str) {
                if (adapter != null) {
                    adapter.getFilter().filter(str);
                }
            }
        });
    }

    private void loadPeerFromIntentData() {
        if (peer != null) { // why?
            return;
        }

        Intent intent = getActivity().getIntent();
        if (intent.hasExtra(Constants.EXTRA_PEER_UUID)) {
            byte[] uuid = intent.getByteArrayExtra(Constants.EXTRA_PEER_UUID);

            if (uuid != null) {
                peer = PeerManager.instance().findPeerByUUID(uuid);
                local = peer.isLocalHost();
            }
        }
    }

    private RadioButton initRadioButton(View v, int viewId, final byte fileType) {
        RadioButton button = findView(v, viewId);
        button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    browseFilesButtonClick(fileType);
                }
            }
        });

        return button;
    }

    private void browseFilesButtonClick(byte fileType) {
        if (adapter != null) {
            adapter.clear();
            adapter = null;
        }

        filesBar.clearCheckAll();
        filesBar.clearSearch();

        getLoaderManager().destroyLoader(LOADER_FILES_ID);
        Bundle bundle = new Bundle();
        bundle.putByte("fileType", fileType);
        getLoaderManager().restartLoader(LOADER_FILES_ID, bundle, this);
    }

    private Loader<Object> createLoaderFinger() {
        AsyncTaskLoader<Object> loader = new AsyncTaskLoader<Object>(getActivity()) {
            @Override
            public Object loadInBackground() {
                try {
                    return peer.finger();
                } catch (Throwable e) {
                    Log.e(TAG, "Error performing finger", e);
                }
                return null;
            }
        };
        loader.forceLoad();
        return loader;
    }

    private Loader<Object> createLoaderFiles(final byte fileType) {
        AsyncTaskLoader<Object> loader = new AsyncTaskLoader<Object>(getActivity()) {
            @Override
            public Object loadInBackground() {
                try {
                    return new Object[] { fileType, peer.browse(fileType) };
                } catch (Throwable e) {
                    Log.e(TAG, "Error performing finger", e);
                }
                return null;
            }
        };
        loader.forceLoad();
        return loader;
    }

    public void updateHeader() {
        if (finger == null) {
            Log.w(TAG, "Something wrong, finger is null");
            UIUtils.showShortMessage(getActivity(), R.string.is_not_responding, peer.getNickname());
            getActivity().finish();
            return;
        }

        int video = finger.numSharedVideoFiles;
        int pictures = finger.numSharedPictureFiles;
        int ringtones = finger.numSharedRingtoneFiles;
        int audio = finger.numSharedAudioFiles;
        int applications = finger.numSharedApplicationFiles;
        int documents = finger.numSharedDocumentFiles;

        setFilesCount(buttonVideos, video, finger.numTotalVideoFiles);
        setFilesCount(buttonPictures, pictures, finger.numTotalPictureFiles);
        setFilesCount(buttonRingtones, ringtones, finger.numTotalRingtoneFiles);
        setFilesCount(buttonAudio, audio, finger.numTotalAudioFiles);
        setFilesCount(buttonApplications, applications, finger.numTotalApplicationFiles);
        setFilesCount(buttonDocuments, documents, finger.numTotalDocumentFiles);
    }

    private void updateFiles(Object[] data) {
        if (data == null) {
            Log.w(TAG, "Something wrong, data is null");
            UIUtils.showShortMessage(getActivity(), R.string.is_not_responding, peer.getNickname());
            getActivity().finish();
            return;
        }

        try {
            byte fileType = (Byte) data[0];

            @SuppressWarnings("unchecked")
            List<FileDescriptor> items = (List<FileDescriptor>) data[1];
            adapter = new FileListAdapter(getActivity(), items, peer, local, fileType) {
                protected void onItemChecked(View v, boolean isChecked) {
                    if (!isChecked) {
                        filesBar.clearCheckAll();
                    }
                }
            };
            adapter.setCheckboxesVisibility(true);
            setListAdapter(adapter);
        } catch (Throwable e) {
            Log.e(TAG, "Error updating files in list", e);
        }
    }

    private void showProgressDialog() {
        hideProgressDialog();

        progressDlg = new ProgressDialog(getActivity());
        progressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDlg.setMessage(getString(R.string.loading_indeterminate));
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

    private void setFilesCount(RadioButton button, int numShared, int numTotal) {
        if (local) {
            button.setText(String.valueOf(numShared) + "/" + String.valueOf(numTotal));
        } else {
            button.setText(String.valueOf(numShared));
        }
    }

    private final class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_MEDIA_PLAYER_STOPPED)) {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } else if (intent.getAction().equals(Constants.ACTION_REFRESH_FINGER)) {
                getLoaderManager().restartLoader(LOADER_FINGER_ID, null, BrowsePeerFragment.this);
            }
        }
    }
}
