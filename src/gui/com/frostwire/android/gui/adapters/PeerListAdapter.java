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

package com.frostwire.android.gui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.Peer;
import com.frostwire.android.gui.SoftwareUpdater;
import com.frostwire.android.gui.activities.BrowsePeerActivity;
import com.frostwire.android.gui.adapters.menu.BrowsePeerMenuAction;
import com.frostwire.android.gui.adapters.menu.ChangeNicknameMenuAction;
import com.frostwire.android.gui.views.AbstractListAdapter;
import com.frostwire.android.gui.views.MenuAction;
import com.frostwire.android.gui.views.MenuAdapter;
import com.frostwire.android.gui.views.ShareIndicationDialog;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class PeerListAdapter extends AbstractListAdapter<Peer> {

    @SuppressWarnings("unused")
    private static final String TAG = "FW.PeerListAdapter";

    private final Drawable libraryDrawable;
    private final Drawable peerDrawable;
    private final Drawable howtoShareDrawable;

    private final OnClickListener howtoShareClickListener;

    public PeerListAdapter(Context context, List<Peer> peers) {
        super(context, R.layout.view_peer_list_item, peers);

        Resources r = getContext().getResources();
        
        libraryDrawable = r.getDrawable(R.drawable.library);
        peerDrawable = r.getDrawable(R.drawable.silhouette_dark);
        howtoShareDrawable = r.getDrawable(R.drawable.share_howto);

        howtoShareClickListener = new OnClickListener() {
            public void onClick(View v) {
                trackDialog(new ShareIndicationDialog(getContext()).show(true));
            }
        };
    }

    @Override
    protected void populateView(View view, Peer peer) {
        TextView title = findView(view, R.id.view_peer_list_item_title);
        title.setText(peer.getNickname());

        TextView version = findView(view, R.id.view_peer_list_item_version);
        version.setText("v. " + peer.getClientVersion());
        version.setTextColor(0xffcccccc);

        ImageView peerIcon = findView(view, R.id.view_peer_list_item_icon);
        peerIcon.setBackgroundDrawable(peer.isLocalHost() ? libraryDrawable : peerDrawable);

        ImageButton howtoShareButton = findView(view, R.id.view_peer_list_item_button_how_to_share);
        howtoShareButton.setOnClickListener(howtoShareClickListener);

        if (!peer.isLocalHost()) {
            howtoShareButton.setVisibility(View.INVISIBLE);
            title.setTextColor(R.color.frostwire_dark_blue);
        } else {
            title.setTextColor(R.color.frostwire_orange);

            // show my version in red If I'm old to encourage user to update.
            if (SoftwareUpdater.instance().isOldVersion()) {
                version.setTextColor(Color.RED);
                version.setText(getContext().getString(R.string.please_update_to_v, SoftwareUpdater.instance().getLatestVersion()));
            }

            howtoShareButton.setVisibility(View.VISIBLE);
            howtoShareButton.setImageDrawable(howtoShareDrawable);
            
        }

        TextView summary = findView(view, R.id.view_peer_list_item_summary);
        summary.setText(getContext().getString(R.string.summary_files_shared, peer.getNumSharedFiles()));
    }

    @Override
    protected void onItemClicked(View v) {
        Peer peer = (Peer) v.getTag();

        if (peer == null) {
            return;
        }

        Intent i = new Intent(getContext(), BrowsePeerActivity.class);
        i.putExtra(Constants.EXTRA_PEER_UUID, peer.getUUID());
        getContext().startActivity(i);
    }

    @Override
    protected MenuAdapter getMenuAdapter(View view) {
        Peer peer = (Peer) view.getTag();

        if (peer == null) {
            return null;
        }

        return buildMenuAdapter(peer);
    }

    private MenuAdapter buildMenuAdapter(Peer peer) {
        Context context = getContext();
        List<MenuAction> items = new ArrayList<MenuAction>();

        if (peer.isLocalHost()) {
            items.add(new ChangeNicknameMenuAction(context, this));
        }

        items.add(new BrowsePeerMenuAction(context, peer));

        return new MenuAdapter(context, peer.getNickname(), items);
    }
}