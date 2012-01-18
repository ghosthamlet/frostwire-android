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

package com.frostwire.android.gui.adapters.menu;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.frostwire.android.R;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.gui.Peer;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.gui.views.AbstractListAdapter;
import com.frostwire.android.gui.views.MenuAction;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class DownloadCheckedMenuAction extends MenuAction {

    private final AbstractListAdapter<?> adapter;
    private final List<FileDescriptor> fds;
    private final Peer peer;

    public DownloadCheckedMenuAction(Context context, AbstractListAdapter<?> adapter, List<FileDescriptor> fds, Peer peer) {
        super(context, R.drawable.download_round_button, context.getResources().getString(R.string.download_selected_files) + " (" + fds.size() + ")");

        this.adapter = adapter;
        this.fds = fds;
        this.peer = peer;
    }

    @Override
    public void onClick() {
        if (fds == null) {
            return;
        }

        final int size = fds.size();

        if (size == 0) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (fds != null && fds.size() > 0) {
                    for (int i = 0; i < size; i++) {
                        TransferManager.instance().download(peer, fds.get(i));
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                try {
                    adapter.notifyDataSetChanged();
                    adapter.clearChecked();
                } catch (Throwable e) {
                    // ignore
                }
            }
        }.execute();

        String message = (fds.size() > 1) ? String.format(getContext().getString(R.string.downloads_added_to_queue), String.valueOf(fds.size())) : getContext().getString(R.string.download_added_to_queue);
        UIUtils.showLongMessage(getContext(), message);
    }
}
