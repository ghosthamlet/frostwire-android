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

package com.frostwire.android.core.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.frostwire.android.core.Constants;
import com.frostwire.android.core.providers.UniversalStore.Torrents;
import com.frostwire.android.core.providers.UniversalStore.Torrents.TorrentFilesColumns;
import com.frostwire.android.util.StringUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentsProvider extends ContentProvider {

    private static final String TAG = "FW.TorrentsProvider";

    private static final String DATABASE_NAME = "torrents.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TORRENT_FILES_TABLE_NAME = "torrent_files";
    private static final String FTS_VIRTUAL_TABLE = "fts_torrent_files";

    private static final int TORRENT_FILES_ALL = 1;
    private static final int TORRENT_FILES_ID = 2;
    private static final int TORRENT_FILES_SEARCH = 3;

    private static final UriMatcher uriMatcher;
    private static HashMap<String, String> projectionMap;

    private DatabaseHelper databaseHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(UniversalStore.UNIVERSAL_TORRENTS_AUTHORITY, "torrentfiles", TORRENT_FILES_ALL);
        uriMatcher.addURI(UniversalStore.UNIVERSAL_TORRENTS_AUTHORITY, "torrentfiles/#", TORRENT_FILES_ID);
        uriMatcher.addURI(UniversalStore.UNIVERSAL_TORRENTS_AUTHORITY, "torrentfiles/search", TORRENT_FILES_SEARCH);

        projectionMap = new HashMap<String, String>();
        projectionMap.put(TorrentFilesColumns._ID, TorrentFilesColumns._ID);
        projectionMap.put(TorrentFilesColumns.TIMESTAMP, TorrentFilesColumns.TIMESTAMP);
        projectionMap.put(TorrentFilesColumns.TORRENT_INFO_HASH, TorrentFilesColumns.TORRENT_INFO_HASH);
        projectionMap.put(TorrentFilesColumns.TORRENT_FILE_NAME, TorrentFilesColumns.TORRENT_FILE_NAME);
        projectionMap.put(TorrentFilesColumns.TORRENT_SEEDS, TorrentFilesColumns.TORRENT_SEEDS);
        projectionMap.put(TorrentFilesColumns.RELATIVE_PATH, TorrentFilesColumns.RELATIVE_PATH);
        projectionMap.put(TorrentFilesColumns.KEYWORDS, TorrentFilesColumns.KEYWORDS);
        projectionMap.put(TorrentFilesColumns.JSON, TorrentFilesColumns.JSON);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
        case TORRENT_FILES_ALL:
            qb.setTables(TORRENT_FILES_TABLE_NAME);
            qb.setProjectionMap(projectionMap);
            break;
        case TORRENT_FILES_ID:
            qb.setTables(TORRENT_FILES_TABLE_NAME);
            qb.setProjectionMap(projectionMap);
            qb.appendWhere(TorrentFilesColumns._ID + "=" + uri.getPathSegments().get(1));
            break;
        case TORRENT_FILES_SEARCH:
            if (selectionArgs == null) {
                throw new IllegalArgumentException("selectionArgs must be provided for the URI: " + uri);
            }
            return ftsSearch(selectionArgs[0], sortOrder);
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Torrents.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data
        // changes
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case TORRENT_FILES_ALL:
            return Torrents.Media.CONTENT_TYPE;
        case TORRENT_FILES_ID:
            return Torrents.Media.CONTENT_TYPE_ITEM;
        case TORRENT_FILES_SEARCH:
            return Torrents.Media.CONTENT_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        // Validate the requested uri
        if (uriMatcher.match(uri) != TORRENT_FILES_ALL) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(TorrentFilesColumns.TIMESTAMP) == false) {
            values.put(TorrentFilesColumns.TIMESTAMP, now);
        }

        if (values.containsKey(TorrentFilesColumns.TORRENT_INFO_HASH) == false) {
            values.put(TorrentFilesColumns.TORRENT_INFO_HASH, "");
        }

        if (values.containsKey(TorrentFilesColumns.TORRENT_FILE_NAME) == false) {
            values.put(TorrentFilesColumns.TORRENT_FILE_NAME, "");
        }

        if (values.containsKey(TorrentFilesColumns.TORRENT_SEEDS) == false) {
            values.put(TorrentFilesColumns.TORRENT_SEEDS, 0);
        }

        if (values.containsKey(TorrentFilesColumns.RELATIVE_PATH) == false) {
            values.put(TorrentFilesColumns.RELATIVE_PATH, "");
        }

        if (values.containsKey(TorrentFilesColumns.KEYWORDS) == false) {
            values.put(TorrentFilesColumns.KEYWORDS, "");
        }

        if (values.containsKey(TorrentFilesColumns.JSON) == false) {
            values.put(TorrentFilesColumns.JSON, "");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        long rowId = db.insert(TORRENT_FILES_TABLE_NAME, "", values);

        if (rowId > 0) {

            purgeOld();
            // insert in fts
            ContentValues cv = new ContentValues();
            cv.put("docid", rowId);
            cv.put(TorrentFilesColumns.KEYWORDS, values.getAsString(TorrentFilesColumns.KEYWORDS));
            cv.put(TorrentFilesColumns.TORRENT_SEEDS, values.getAsInteger(TorrentFilesColumns.TORRENT_SEEDS));
            db.insert(FTS_VIRTUAL_TABLE, "", cv);

            Uri insertUri = ContentUris.withAppendedId(Torrents.Media.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(insertUri, null);

            return insertUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
        case TORRENT_FILES_ALL:
            count = deleteTorrentFiles(where, whereArgs);
            break;
        case TORRENT_FILES_ID:
            String documentId = uri.getPathSegments().get(1);
            count = deleteTorrentFiles(TorrentFilesColumns._ID + "=" + documentId + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : ""), whereArgs);
            break;
        case TORRENT_FILES_SEARCH:
            count = deleteFts(where, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        throw new UnsupportedOperationException();
    }

    private Cursor ftsSearch(String query, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FTS_VIRTUAL_TABLE);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] columns = new String[] { "rowid" };
        String selection = TorrentFilesColumns.KEYWORDS + " MATCH ?";
        String[] selectionArgs = new String[] { query };

        return qb.query(db, columns, selection, selectionArgs, null, null, sortOrder);
    }

    private int deleteTorrentFiles(String where, String[] whereArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor c = null;
        List<Integer> ids = new ArrayList<Integer>();
        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TORRENT_FILES_TABLE_NAME);
            qb.setProjectionMap(projectionMap);
            c = qb.query(db, new String[] { TorrentFilesColumns._ID }, where, whereArgs, null, null, null);
            while (c.moveToNext()) {
                int id = c.getInt(0);
                ids.add(id);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        int n = db.delete(FTS_VIRTUAL_TABLE, "rowid IN " + StringUtils.buildSet(ids), null);
        Log.d(TAG, String.format("Deleted from FTS_VIRTUAL_TABLE %d elements", n));

        return db.delete(TORRENT_FILES_TABLE_NAME, where, whereArgs);
    }

    private int deleteFts(String where, String[] whereArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int n = db.delete(FTS_VIRTUAL_TABLE, where, whereArgs);
        Log.d(TAG, String.format("Deleted from FTS_VIRTUAL_TABLE %d elements", n));
        return n;
    }

    private void purgeOld() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor c = null;
        List<Integer> ids = new ArrayList<Integer>();
        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TORRENT_FILES_TABLE_NAME);
            qb.setProjectionMap(projectionMap);
            c = qb.query(db, new String[] { TorrentFilesColumns._ID }, null, null, null, null, "timestamp ASC");
            int count = c.getCount() - Constants.MAX_INDEXED_TORRENT_SUB_FILES;
            for (int i = 0; i < count && c.moveToNext(); i++) {
                int id = c.getInt(0);
                ids.add(id);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (ids.size() > (Constants.MAX_INDEXED_TORRENT_SUB_FILES * 0.016)) {
            Log.d(TAG, String.format("Need to purge %d old indexed torrents", ids.size()));
            String set = StringUtils.buildSet(ids);

            int n = db.delete(FTS_VIRTUAL_TABLE, "rowid IN " + set, null);
            Log.d(TAG, String.format("Deleted from FTS_VIRTUAL_TABLE %d elements", n));

            n = db.delete(TORRENT_FILES_TABLE_NAME, "_id IN " + set, null);
            Log.d(TAG, String.format("Deleted from TORRENT_FILES_TABLE_NAME %d elements", n));
        }
    }

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TORRENT_FILES_TABLE_NAME + " (" + TorrentFilesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TorrentFilesColumns.TIMESTAMP + " INTEGER," + TorrentFilesColumns.TORRENT_INFO_HASH + " TEXT," + TorrentFilesColumns.TORRENT_FILE_NAME + " TEXT,"
                    + TorrentFilesColumns.TORRENT_SEEDS + " INTEGER," + TorrentFilesColumns.RELATIVE_PATH + " TEXT," + TorrentFilesColumns.KEYWORDS + " TEXT," + TorrentFilesColumns.JSON + " TEXT" + ");");
            db.execSQL("CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE + " USING fts3 (" + TorrentFilesColumns.KEYWORDS + " TEXT," + TorrentFilesColumns.TORRENT_SEEDS + " INTEGER" + ", tokenize=porter);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading torrents database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TORRENT_FILES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }
}
