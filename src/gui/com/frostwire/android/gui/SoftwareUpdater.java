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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Locale;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.frostwire.android.core.Constants;
import com.frostwire.android.core.CoreRuntimeException;
import com.frostwire.android.core.HttpFetcher;
import com.frostwire.android.gui.activities.MainActivity;
import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.android.util.JsonUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class SoftwareUpdater {

    private static final String TAG = "FW.SoftwareUpdater";

    private final Application context;

    private boolean oldVersion;
    private String latestVersion;
    private String updateMessage;

    private static SoftwareUpdater instance;

    public synchronized static void create(Application context) {
        if (instance != null) {
            return;
        }
        instance = new SoftwareUpdater(context);
    }

    public static SoftwareUpdater instance() {
        if (instance == null) {
            throw new CoreRuntimeException("SoftwareUpdater not created");
        }
        return instance;
    }

    private SoftwareUpdater(Application context) {
        this.context = context;

        this.oldVersion = false;
        this.latestVersion = Constants.FROSTWIRE_VERSION_STRING;
        this.updateMessage = null;
    }

    public boolean isOldVersion() {
        return oldVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public void checkForUpdate() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    SystemClock.sleep(2000);

                    byte[] jsonBytes = new HttpFetcher(Constants.SERVER_UPDATE_URL).fetch();
                    Update update = JsonUtils.toObject(new String(jsonBytes), Update.class);

                    latestVersion = update.v;
                    String[] latestVersionArr = latestVersion.split("\\.");

                    // lv = latest version
                    byte[] lv = new byte[] { Byte.valueOf(latestVersionArr[0]), Byte.valueOf(latestVersionArr[1]), Byte.valueOf(latestVersionArr[2]) };

                    // mv = my version
                    byte[] mv = Constants.FROSTWIRE_VERSION;

                    oldVersion = isFrostWireOld(mv, lv);

                    updateConfiguration(update);

                    if (oldVersion) {
                        // did we download the newest already?
                        if (downloadedLatestFrostWire(update.md5)) {
                            return true;
                        }
                        // didn't download it? go get it now
                        else {
                            new HttpFetcher(update.u).save(SystemUtils.getUpdateInstallerPath());

                            if (downloadedLatestFrostWire(update.md5)) {
                                return true;
                            }
                        }
                    }

                } catch (Throwable e) {
                    Log.e(TAG, "Failed to check or retrieve the update information", e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    notifyUpdate();
                }
            }
        };
    }

    private void notifyUpdate() {
        try {
            Intent i = new Intent(context, MainActivity.class);

            i.setAction(Constants.ACTION_ADVICE_UPDATE);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        } catch (Throwable e) {
            Log.e(TAG, "Failed to notify update", e);
        }
    }

    /**
     * 
     * @param md5
     *            - Expected MD5 hash as a string.
     * @return
     */
    private boolean downloadedLatestFrostWire(String md5) {
        if (!SystemUtils.getUpdateInstallerPath().exists()) {
            return false;
        }
        return checkMD5(SystemUtils.getUpdateInstallerPath(), md5);
    }

    /**
     * mv = my version
     * lv = latest version
     * 
     * returns true if mv is older.
     */
    private boolean isFrostWireOld(byte[] mv, byte[] lv) {
        if (mv[0] < lv[0]) {
            return true;
        }

        if (mv[0] == lv[0] && mv[1] < lv[1]) {
            return true;
        }

        if (mv[0] == lv[0] && mv[1] == lv[1] && mv[2] < lv[2]) {
            return true;
        }

        return false;
    }

    private static String getMD5(File f) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");

            // We read the file in buffers so we don't
            // eat all the memory in case we have a huge plugin.
            byte[] buf = new byte[65536];
            int num_read;

            InputStream in = new BufferedInputStream(new FileInputStream(f));

            while ((num_read = in.read(buf)) != -1) {
                m.update(buf, 0, num_read);
            }

            in.close();

            String result = new BigInteger(1, m.digest()).toString(16);

            // pad with zeros if until it's 32 chars long.
            if (result.length() < 32) {
                int paddingSize = 32 - result.length();
                for (int i = 0; i < paddingSize; i++) {
                    result = "0" + result;
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean checkMD5(File f, String expectedMD5) {
        if (expectedMD5 == null) {
            return false;
        }

        if (expectedMD5.length() != 32) {
            return false;
        }

        String checkedMD5 = getMD5(f);
        if (checkedMD5 == null) {
            return false;
        }

        return checkedMD5.trim().equalsIgnoreCase(expectedMD5.trim());
    }

    private void updateConfiguration(Update update) {
        if (update.config == null) {
            return;
        }

        if (update.config.updateMessageLanguages != null && update.config.updateMessages != null) {
            String language = Locale.getDefault().getLanguage();
            for (int i = 0; i < update.config.updateMessageLanguages.length; i++) {
                if (update.config.updateMessageLanguages[i].equals(language)) {
                    updateMessage = update.config.updateMessages[i];
                }
            }
        }
    }

    private static class Update {
        public String v;
        public String u;
        public String md5;
        public Config config;
    }

    private static class Config {
        public String[] updateMessageLanguages;
        public String[] updateMessages;
    }
}
