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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.frostwire.android.R;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.gui.adapters.FileListAdapter;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.gui.views.MenuAction;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ToggleFileGrainedSharingMenuAction extends MenuAction {

    private final FileListAdapter adapter;
    private final List<FileDescriptor> fds;

    public ToggleFileGrainedSharingMenuAction(Context context, FileListAdapter adapter, List<FileDescriptor> fds) {
        super(context, R.drawable.unlocked, context.getResources().getString(R.string.toggle_sharing_selected_files) + (fds.size() > 1 ? " (" + fds.size() + ")" : ""));
        this.adapter = adapter;
        this.fds = fds;
    }

    @Override
    public void onClick() {
        boolean sharing = false;

        //toggle everybody in memory (fast)
        int size = fds.size();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            FileDescriptor fd = fds.get(i);
            fd.shared = !fd.shared;
            if (fd.shared) {
                sharing = true;
            }
        }

        adapter.notifyDataSetChanged();

        final byte fileType = fds.get(0).fileType;

        final boolean showMessage = sharing;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                //we pass a copy of the list of FileDescriptor in order to avoid inconsistencies
                Librarian.instance().updateSharedStates(fileType, new ArrayList<FileDescriptor>(fds));
                return null;
            }

            protected void onPostExecute(Void result) {
                try {
                    if (showMessage) {
                        int numShared = Librarian.instance().getNumFiles(fileType, true);
                        if (numShared > 1) {
                            UIUtils.showLongMessage(getContext(), getContext().getString(R.string.sharing_num_files, numShared, UIUtils.getFileTypeAsString(getContext().getResources(), fileType)));
                        }
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }
        }.execute();
    }
}
