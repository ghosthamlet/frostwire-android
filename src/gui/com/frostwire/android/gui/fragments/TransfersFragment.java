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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.view.View;

import com.frostwire.android.R;
import com.frostwire.android.gui.adapters.TransferListAdapter;
import com.frostwire.android.gui.transfers.Transfer;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.views.AbstractActivity;
import com.frostwire.android.gui.views.AbstractExpandableListFragment;
import com.frostwire.android.gui.views.Refreshable;
import com.frostwire.android.gui.views.TransfersBarView;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class TransfersFragment extends AbstractExpandableListFragment implements Refreshable {

    @SuppressWarnings("unused")
    private static final String TAG = "FW.TransfersFragment";

    private final Comparator<Transfer> transferComparator;

    private TransfersBarView bar;
    private TransferListAdapter adapter;

    public TransfersFragment() {
        super(R.layout.fragment_transfers);

        this.transferComparator = new TransferComparator();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        if (getActivity() instanceof AbstractActivity) {
            ((AbstractActivity) getActivity()).addRefreshable(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (adapter != null) {
            adapter.dismissDialogs();
        }
    }

    public void refresh() {
        if (adapter != null) {
            List<Transfer> transfers = TransferManager.instance().getTransfers();
            Collections.sort(transfers, transferComparator);
            adapter.updateList(transfers);
        } else {
            setupAdapter();
        }

        bar.refresh();
    }

    @Override
    protected void initComponents(View v) {
        bar = findView(v, R.id.fragment_transfers_bar);
    }

    private void setupAdapter() {
        List<Transfer> transfers = TransferManager.instance().getTransfers();
        Collections.sort(transfers, transferComparator);
        adapter = new TransferListAdapter(this.getActivity(), transfers);
        setListAdapter(adapter);
    }

    private static final class TransferComparator implements Comparator<Transfer> {
        public int compare(Transfer lhs, Transfer rhs) {
            try {
                return -lhs.getDateCreated().compareTo(rhs.getDateCreated());
            } catch (Throwable e) {
                // ignore, not really super important
            }
            return 0;
        }
    }
}
