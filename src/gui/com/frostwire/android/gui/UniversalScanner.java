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

import java.io.File;
import java.util.Arrays;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.core.providers.TableFetcher;
import com.frostwire.android.core.providers.TableFetchers;
import com.frostwire.android.core.providers.UniversalStore;
import com.frostwire.android.core.providers.UniversalStore.Documents;
import com.frostwire.android.core.providers.UniversalStore.Documents.DocumentsColumns;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.util.FilenameUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
final class UniversalScanner {

    private static final String TAG = "FW.UniversalScanner";

    private Context context;

    public UniversalScanner(Context context) {
        this.context = context;
    }

    public void scan(String filePath) {
        new AndroidScanner(filePath).scan();
    }

    private static void shareFinishedDownload(FileDescriptor fd) {
        if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TRANSFER_SHARE_FINISHED_DOWNLOADS)) {
            fd.shared = true;
            Librarian.instance().updateSharedStates(fd.fileType, Arrays.asList(fd));
        }
        Librarian.instance().invalidateCountCache(fd.fileType);
    }

    private void scanDocument(String filePath) {
        File file = new File(filePath);

        if (documentExists(filePath, file.length())) {
            return;
        }

        String displayName = FilenameUtils.getBaseName(file.getName());

        ContentResolver cr = context.getContentResolver();

        ContentValues values = new ContentValues();

        values.put(DocumentsColumns.DATA, filePath);
        values.put(DocumentsColumns.SIZE, file.length());
        values.put(DocumentsColumns.DISPLAY_NAME, displayName);
        values.put(DocumentsColumns.TITLE, displayName);
        values.put(DocumentsColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(DocumentsColumns.DATE_MODIFIED, file.lastModified());
        values.put(DocumentsColumns.MIME_TYPE, UIUtils.getMimeType(filePath));

        Uri uri = cr.insert(Documents.Media.CONTENT_URI, values);

        FileDescriptor fd = new FileDescriptor();
        fd.fileType = Constants.FILE_TYPE_DOCUMENTS;
        fd.id = Integer.valueOf(uri.getLastPathSegment());

        shareFinishedDownload(fd);
    }

    private boolean documentExists(String filePath, long size) {
        boolean result = false;

        Cursor c = null;

        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(UniversalStore.Documents.Media.CONTENT_URI, new String[] { DocumentsColumns._ID }, DocumentsColumns.DATA + "=?" + " AND " + DocumentsColumns.SIZE + "=?", new String[] { filePath, String.valueOf(size) }, null);
            result = c.getCount() != 0;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }

    private final class AndroidScanner implements MediaScannerConnectionClient {

        private final String path;

        private MediaScannerConnection connection;

        public AndroidScanner(String path) {
            this.path = path;
        }

        public void scan() {
            try {
                connection = new MediaScannerConnection(context, this);
                connection.connect();
            } catch (Throwable e) {
                Log.e(TAG, "Error scanning file with android internal scanner, one retry", e);
                SystemClock.sleep(1000);
                connection = new MediaScannerConnection(context, this);
                connection.connect();
            }
        }

        public void onMediaScannerConnected() {
            connection.scanFile(path, null);
        }

        public void onScanCompleted(String path, Uri uri) {
            connection.disconnect();

            if (uri != null) {
                //Log.d(TAG, "Scanned new file: " + uri);

                TableFetcher fetcher = TableFetchers.getFetcher(uri);

                FileDescriptor fd = new FileDescriptor();
                fd.fileType = fetcher.getFileType();
                fd.id = Integer.valueOf(uri.getLastPathSegment());

                shareFinishedDownload(fd);
            } else {
                if (path.endsWith(".apk")) {
                    //Log.d(TAG, "Can't scan apk for security concerns: " + path);
                } else {
                    scanDocument(path);
                    //Log.d(TAG, "Scanned new file as document: " + path);
                }
            }
        }
    }
}
