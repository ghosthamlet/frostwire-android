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

package com.frostwire.android.gui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.activities.MainActivity;
import com.frostwire.android.gui.transfers.BittorrentPromotionSearchResult;
import com.frostwire.android.gui.transfers.DownloadTransfer;
import com.frostwire.android.gui.transfers.InvalidTransfer;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.gui.views.NewTransferDialog;
import com.frostwire.android.gui.views.NewTransferDialog.OnYesNoListener;
import com.frostwire.android.util.JsonUtils;
import com.frostwire.android.util.StringUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class PromotionsHandler {

    private static final String TAG = "FW.PromotionHandler";

    private final Context context;

    public PromotionsHandler(Context context) {
        this.context = context;
    }

    public void handleSelection(String json) {
        try {
            json = StringUtils.unescape(json);
            Slide slide = JsonUtils.toObject(json, Slide.class);
            startTransfer(slide);
        } catch (Throwable e) {
            Log.e(TAG, "Error processing promotion", e);
        }
    }

    private void startTransfer(final Slide slide) {
        final BittorrentPromotionSearchResult bittorrentSearchResult = new BittorrentPromotionSearchResult(slide);
        
        NewTransferDialog dlg = new NewTransferDialog(context,bittorrentSearchResult, false, new OnYesNoListener() {
            public void onYes(NewTransferDialog dialog) {
                try {
                    DownloadTransfer download = TransferManager.instance().download(bittorrentSearchResult);
                    if (!(download instanceof InvalidTransfer)) {
                        UIUtils.showShortMessage(context, R.string.downloading_promotion, download.getDisplayName());
                        if (ConfigurationManager.instance().showTransfersOnDownloadStart()) {
                            Intent i = new Intent(Constants.ACTION_SHOW_TRANSFERS);
                            context.startActivity(i.setClass(context, MainActivity.class));
                        }
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Error processing promotion", e);
                }
            }

            public void onNo(NewTransferDialog dialog) {
            }
        });

        dlg.show(); // this dialog will leak if the context is destroyed. Find a solution.
    }

    public static class Slide {

        /**
         * http address where to go if user clicks on this slide
         */
        public String url;

        /**
         * url of torrent file that should be opened if user clicks on this slide
         */
        public String torrent;

        /**
         * url of image that will be displayed on this slide
         */
        public String imageSrc;

        /**
         * length of time this slide will be shown
         */
        public long duration;

        /**
         * language (optional filter) = Can be given in the forms of:
         * *
         * en
         * en_US
         * 
         */
        public String language;

        /**
         * os (optional filter) = Can be given in the forms of:
         * windows
         * mac
         * linux
         */
        public String os;
        
        /** Title of the promotion */
        public String title;
        
        /** Total size in bytes */
        public long size;
    }
}
