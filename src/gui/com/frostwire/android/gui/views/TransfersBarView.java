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
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.Peer;
import com.frostwire.android.gui.PeerManager;
import com.frostwire.android.gui.activities.BrowsePeerActivity;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.UIUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TransfersBarView extends RelativeLayout implements Refreshable {

    private Button buttonPauseAll;
    private Button buttonClearComplete;
    private Button buttonMyFiles;
    private TextView textDownloads;
    private TextView textUploads;

    public TransfersBarView(Context context, AttributeSet set) {
        super(context, set);

    }

    public void refresh() {
        //  format strings
        String sDown = UIUtils.rate2speed(TransferManager.instance().getDownloadsBandwidth());
        String sUp = UIUtils.rate2speed(TransferManager.instance().getUploadsBandwidth());

        // number of uploads (seeding) and downloads
        int downloads = TransferManager.instance().getActiveDownloads();
        int uploads = TransferManager.instance().getActiveUploads();

        textDownloads.setText(downloads + " @ " + sDown);
        textUploads.setText(uploads + " @ " + sUp);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_transfers_bar, this);

        buttonPauseAll = (Button) findViewById(R.id.view_transfers_bar_pause_all_button);
        buttonPauseAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UIUtils.showYesNoDialog(getContext(), R.string.stop_all_transfers , R.string.are_you_sure, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TransferManager.instance().pauseTorrents();
                    }
                });
            }
        });
        buttonClearComplete = (Button) findViewById(R.id.view_transfers_bar_clear_complete_button);
        buttonClearComplete.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UIUtils.showYesNoDialog(getContext(), R.string.clear_complete_transfers , R.string.are_you_sure, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TransferManager.instance().clearComplete();
                    }
                });
            }
        });
        buttonMyFiles = (Button) findViewById(R.id.view_transfers_bar_my_files_button);
        buttonMyFiles.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Peer peer = PeerManager.instance().getLocalPeer();
                Intent i = new Intent(getContext(), BrowsePeerActivity.class);
                i.putExtra(Constants.EXTRA_PEER_UUID, peer.getUUID());
                getContext().startActivity(i);
            }
        });

        textDownloads = (TextView) findViewById(R.id.view_transfers_bar_downloads_indicator_text);
        textUploads = (TextView) findViewById(R.id.view_transfers_bar_uploads_indicator_text);
    }
}
