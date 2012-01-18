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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TOS {

    private static String NL = System.getProperty("line.separator");

    /**
     * Displays the TOS if necessary. This method should be called from the onCreate()
     * method of your main Activity.
     */
    public static AlertDialog showEula(Context context, final OnTOSAcceptListener listener) {
        if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_TOS_ACCEPTED)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.tos_title);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.tos_accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ConfigurationManager.instance().setBoolean(Constants.PREF_KEY_GUI_TOS_ACCEPTED, true);
                    listener.onAccept();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.tos_refuse, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(1);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    System.exit(1);
                }
            });

            builder.setMessage(readFile(context, R.raw.tos));
            AlertDialog dialog = builder.create();
            dialog.show();
            return dialog;
        } else {
            return null;
        }
    }

    private static String readFile(Context context, int id) {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id)));

            String line;
            StringBuilder buffer = new StringBuilder();

            while ((line = in.readLine()) != null) {
                buffer.append(line).append(NL);
            }

            return buffer.toString();

        } catch (Throwable e) {
            return "";
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public interface OnTOSAcceptListener {
        public void onAccept();
    }
}