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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.gui.Peer;
import com.frostwire.android.gui.PeerManager;
import com.frostwire.android.gui.views.AbstractListAdapter;
import com.frostwire.android.gui.views.MenuAction;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ChangeNicknameMenuAction extends MenuAction {

    private AbstractListAdapter<Peer> adapter;

    public ChangeNicknameMenuAction(Context context, AbstractListAdapter<Peer> adapter) {
        super(context, R.drawable.user_icon, R.string.change_my_nickname);

        this.adapter = adapter;
    }

    /**
     * This will not only change the nick name in the preferences, it will also:
     * - Update the item on the PeerListAdapter.
     */
    @Override
    public void onClick() {

        Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getText());
        final EditText nickEdit = new EditText(getContext());
        nickEdit.setSingleLine();

        builder.setView(nickEdit);

        String oldNickname = ConfigurationManager.instance().getNickname();
        nickEdit.setText(oldNickname);

        builder.setIcon(R.drawable.application_icon);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (nickEdit.getText().length() == 0) {
                    return;
                }

                String newNick = nickEdit.getText().toString().trim();

                ConfigurationManager.instance().setNickname(newNick);

                if (adapter != null) {
                    List<Peer> peers = adapter.getList();
                    int size = peers.size();
                    for (int i = 0; i < size; i++) {
                        Peer p = peers.get(i);
                        if (p != null && p.isLocalHost()) {
                            p.setNickname(newNick);
                            break;
                        }
                    }
                    PeerManager.instance().getLocalPeer().setNickname(newNick);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        builder.show();
    }
}