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

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video.VideoColumns;

import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.providers.UniversalStore.Applications.ApplicationsColumns;
import com.frostwire.android.core.providers.UniversalStore.Documents.DocumentsColumns;

/**
 * Help yourself with TableFetchers.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class TableFetchers {

    public final static TableFetcher AUDIO_TABLE_FETCHER = new AudioTableFetcher();
    public final static TableFetcher PICTURES_TABLE_FETCHER = new PicturesTableFetcher();
    public final static TableFetcher VIDEOS_TABLE_FETCHER = new VideosTableFetcher();
    public final static TableFetcher DOCUMENTS_TABLE_FETCHER = new DocumentsTableFetcher();
    public final static TableFetcher APPLICATIONS_TABLE_FETCHER = new ApplicationsTableFetcher();
    public final static TableFetcher RINGTONES_TABLE_FETCHER = new RingtonesTableFetcher();

    /**
     * Default Table Fetcher for Audio Files.
     * 
     */
    public final static class AudioTableFetcher implements TableFetcher {

        private int idCol;
        private int pathCol;
        private int mimeCol;
        private int artistCol;
        private int titleCol;
        private int albumCol;
        private int yearCol;
        private int sizeCol;

        public String[] getColumns() {
            return new String[] { AudioColumns._ID, AudioColumns.ARTIST, AudioColumns.TITLE, AudioColumns.ALBUM, AudioColumns.DATA, AudioColumns.YEAR, AudioColumns.MIME_TYPE, AudioColumns.SIZE };
        }

        public String getSortByExpression() {
            return AudioColumns.DATE_ADDED + " DESC";
        }

        public Uri getContentUri() {
            return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        public void prepare(Cursor cur) {
            idCol = cur.getColumnIndex(AudioColumns._ID);
            pathCol = cur.getColumnIndex(AudioColumns.DATA);
            mimeCol = cur.getColumnIndex(AudioColumns.MIME_TYPE);
            artistCol = cur.getColumnIndex(AudioColumns.ARTIST);
            titleCol = cur.getColumnIndex(AudioColumns.TITLE);
            albumCol = cur.getColumnIndex(AudioColumns.ALBUM);
            yearCol = cur.getColumnIndex(AudioColumns.YEAR);
            sizeCol = cur.getColumnIndex(AudioColumns.SIZE);
        }

        public FileDescriptor fetch(Cursor cur) {
            int id = cur.getInt(idCol);
            String path = cur.getString(pathCol);
            String mime = cur.getString(mimeCol);
            String artist = cur.getString(artistCol);
            String title = cur.getString(titleCol);
            String album = cur.getString(albumCol);
            String year = cur.getString(yearCol);
            int size = cur.getInt(sizeCol);

            return new FileDescriptor(Integer.valueOf(id), artist, title, album, year, path, Constants.FILE_TYPE_AUDIO, mime, 0, size, true);
        }

        public byte getFileType() {
            return Constants.FILE_TYPE_AUDIO;
        }
    }

    public static class PicturesTableFetcher implements TableFetcher {

        private int idCol;
        private int titleCol;
        private int pathCol;
        private int mimeCol;
        private int thumbnailCol;
        private int sizeCol;

        public FileDescriptor fetch(Cursor cur) {
            int id = cur.getInt(idCol);
            String path = cur.getString(pathCol);
            String mime = cur.getString(mimeCol);
            String title = cur.getString(titleCol);
            int thumbnailId = cur.getInt(thumbnailCol);
            int size = cur.getInt(sizeCol);

            return new FileDescriptor(Integer.valueOf(id), null, title, null, null, path, Constants.FILE_TYPE_PICTURES, mime, thumbnailId, size, true);
        }

        public String[] getColumns() {
            return new String[] { ImageColumns._ID, ImageColumns.TITLE, ImageColumns.DATA, ImageColumns.MIME_TYPE, ImageColumns.MINI_THUMB_MAGIC, ImageColumns.SIZE };
        }

        public Uri getContentUri() {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        public byte getFileType() {
            return Constants.FILE_TYPE_PICTURES;
        }

        public String getSortByExpression() {
            return ImageColumns.DATE_ADDED + " DESC";
        }

        public void prepare(Cursor cur) {
            idCol = cur.getColumnIndex(ImageColumns._ID);
            titleCol = cur.getColumnIndex(ImageColumns.TITLE);
            pathCol = cur.getColumnIndex(ImageColumns.DATA);
            mimeCol = cur.getColumnIndex(ImageColumns.MIME_TYPE);
            thumbnailCol = cur.getColumnIndex(ImageColumns.MINI_THUMB_MAGIC);
            sizeCol = cur.getColumnIndex(ImageColumns.SIZE);
        }
    }

    public static final class VideosTableFetcher implements TableFetcher {

        private int idCol;
        private int pathCol;
        private int mimeCol;
        private int artistCol;
        private int titleCol;
        private int albumCol;
        private int thumbnailCol;
        private int sizeCol;

        public FileDescriptor fetch(Cursor cur) {
            int id = cur.getInt(idCol);
            String path = cur.getString(pathCol);
            String mime = cur.getString(mimeCol);
            String artist = cur.getString(artistCol);
            String title = cur.getString(titleCol);
            String album = cur.getString(albumCol);
            int thumbnailId = cur.getInt(thumbnailCol);
            int size = cur.getInt(sizeCol);

            return new FileDescriptor(id, artist, title, album, null, path, Constants.FILE_TYPE_VIDEOS, mime, thumbnailId, size, true);
        }

        public String[] getColumns() {
            return new String[] { VideoColumns._ID, VideoColumns.ARTIST, VideoColumns.TITLE, VideoColumns.ALBUM, VideoColumns.DATA, VideoColumns.MIME_TYPE, VideoColumns.MINI_THUMB_MAGIC, VideoColumns.SIZE };
        }

        public Uri getContentUri() {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        public byte getFileType() {
            return Constants.FILE_TYPE_VIDEOS;
        }

        public String getSortByExpression() {
            return VideoColumns.DATE_ADDED + " DESC";
        }

        public void prepare(Cursor cur) {
            idCol = cur.getColumnIndex(VideoColumns._ID);
            pathCol = cur.getColumnIndex(VideoColumns.DATA);
            mimeCol = cur.getColumnIndex(VideoColumns.MIME_TYPE);
            artistCol = cur.getColumnIndex(VideoColumns.ARTIST);
            titleCol = cur.getColumnIndex(VideoColumns.TITLE);
            albumCol = cur.getColumnIndex(VideoColumns.ALBUM);
            thumbnailCol = cur.getColumnIndex(VideoColumns.MINI_THUMB_MAGIC);
            sizeCol = cur.getColumnIndex(VideoColumns.SIZE);
        }
    }

    public static final class DocumentsTableFetcher implements TableFetcher {

        private int idCol;
        private int pathCol;
        private int mimeCol;
        private int titleCol;
        private int sizeCol;

        public FileDescriptor fetch(Cursor cur) {
            int id = cur.getInt(idCol);
            String path = cur.getString(pathCol);
            String mime = cur.getString(mimeCol);
            String title = cur.getString(titleCol);
            int size = cur.getInt(sizeCol);

            return new FileDescriptor(Integer.valueOf(id), null, title, null, null, path, Constants.FILE_TYPE_DOCUMENTS, mime, 0, size, true);
        }

        public String[] getColumns() {
            return new String[] { DocumentsColumns._ID, DocumentsColumns.DATA, DocumentsColumns.SIZE, DocumentsColumns.TITLE, DocumentsColumns.MIME_TYPE };
        }

        public Uri getContentUri() {
            return UniversalStore.Documents.Media.CONTENT_URI;
        }

        public byte getFileType() {
            return Constants.FILE_TYPE_VIDEOS;
        }

        public String getSortByExpression() {
            return VideoColumns.DATE_ADDED + " DESC";
        }

        public void prepare(Cursor cur) {
            idCol = cur.getColumnIndex(VideoColumns._ID);
            pathCol = cur.getColumnIndex(VideoColumns.DATA);
            mimeCol = cur.getColumnIndex(VideoColumns.MIME_TYPE);
            titleCol = cur.getColumnIndex(VideoColumns.TITLE);
            sizeCol = cur.getColumnIndex(VideoColumns.SIZE);
        }
    }

    public static final class ApplicationsTableFetcher implements TableFetcher {

        private int idCol;
        private int titleCol;
        private int verCol;
        private int pathCol;
        private int sizeCol;
        private int packageNameCol;

        public FileDescriptor fetch(Cursor cur) {
            int id = Integer.valueOf(cur.getString(idCol));
            String ver = cur.getString(verCol);
            String title = cur.getString(titleCol);
            String packageName = cur.getString(packageNameCol);
            String path = cur.getString(pathCol);
            int size = Integer.valueOf(cur.getString(sizeCol));

            return new FileDescriptor(id, ver, title, packageName, null, path, Constants.FILE_TYPE_APPLICATIONS, Constants.MIME_TYPE_ANDROID_PACKAGE_ARCHIVE, 0, size, true);
        }

        public String[] getColumns() {
            return new String[] { ApplicationsColumns._ID, ApplicationsColumns.TITLE, ApplicationsColumns.VERSION, ApplicationsColumns.DATA, ApplicationsColumns.SIZE, ApplicationsColumns.PACKAGE_NAME };
        }

        public Uri getContentUri() {
            return UniversalStore.Applications.Media.CONTENT_URI;
        }

        public byte getFileType() {
            return Constants.FILE_TYPE_APPLICATIONS;
        }

        public String getSortByExpression() {
            return "";
        }

        public void prepare(Cursor cur) {
            idCol = cur.getColumnIndex(ApplicationsColumns._ID);
            titleCol = cur.getColumnIndex(ApplicationsColumns.TITLE);
            verCol = cur.getColumnIndex(ApplicationsColumns.VERSION);
            pathCol = cur.getColumnIndex(ApplicationsColumns.DATA);
            sizeCol = cur.getColumnIndex(ApplicationsColumns.SIZE);
            packageNameCol = cur.getColumnIndex(ApplicationsColumns.PACKAGE_NAME);
        }
    }

    public static final class RingtonesTableFetcher implements TableFetcher {

        private int idCol;
        private int pathCol;
        private int mimeCol;
        private int artistCol;
        private int titleCol;
        private int albumCol;
        private int yearCol;
        private int sizeCol;

        public FileDescriptor fetch(Cursor cur) {
            int id = cur.getInt(idCol);
            String path = cur.getString(pathCol);
            String mime = cur.getString(mimeCol);
            String artist = cur.getString(artistCol);
            String title = cur.getString(titleCol);
            String album = cur.getString(albumCol);
            String year = cur.getString(yearCol);
            int size = cur.getInt(sizeCol);

            return new FileDescriptor(Integer.valueOf(id), artist, title, album, year, path, Constants.FILE_TYPE_RINGTONES, mime, 0, size, true);
        }

        public String[] getColumns() {
            return new String[] { AudioColumns._ID, AudioColumns.ARTIST, AudioColumns.TITLE, AudioColumns.ALBUM, AudioColumns.DATA, AudioColumns.YEAR, AudioColumns.MIME_TYPE, AudioColumns.SIZE };
        }

        public Uri getContentUri() {
            return MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        }

        public byte getFileType() {
            return Constants.FILE_TYPE_RINGTONES;
        }

        public String getSortByExpression() {
            return AudioColumns.DATE_ADDED + " DESC";
        }

        public void prepare(Cursor cur) {
            idCol = cur.getColumnIndex(AudioColumns._ID);
            pathCol = cur.getColumnIndex(AudioColumns.DATA);
            mimeCol = cur.getColumnIndex(AudioColumns.MIME_TYPE);
            artistCol = cur.getColumnIndex(AudioColumns.ARTIST);
            titleCol = cur.getColumnIndex(AudioColumns.TITLE);
            albumCol = cur.getColumnIndex(AudioColumns.ALBUM);
            yearCol = cur.getColumnIndex(AudioColumns.YEAR);
            sizeCol = cur.getColumnIndex(AudioColumns.SIZE);
        }
    }

    public static TableFetcher getFetcher(byte fileType) {
        switch (fileType) {
        case Constants.FILE_TYPE_AUDIO:
            return AUDIO_TABLE_FETCHER;
        case Constants.FILE_TYPE_PICTURES:
            return PICTURES_TABLE_FETCHER;
        case Constants.FILE_TYPE_VIDEOS:
            return VIDEOS_TABLE_FETCHER;
        case Constants.FILE_TYPE_DOCUMENTS:
            return DOCUMENTS_TABLE_FETCHER;
        case Constants.FILE_TYPE_APPLICATIONS:
            return APPLICATIONS_TABLE_FETCHER;
        case Constants.FILE_TYPE_RINGTONES:
            return RINGTONES_TABLE_FETCHER;
        default:
            return null;
        }
    }

    public static TableFetcher getFetcher(Uri uri) {
        String str = uri.toString();
        if (str.startsWith(AUDIO_TABLE_FETCHER.getContentUri().toString())) {
            return AUDIO_TABLE_FETCHER;
        } else if (str.startsWith(PICTURES_TABLE_FETCHER.getContentUri().toString())) {
            return PICTURES_TABLE_FETCHER;
        } else if (str.startsWith(VIDEOS_TABLE_FETCHER.getContentUri().toString())) {
            return VIDEOS_TABLE_FETCHER;
        } else if (str.startsWith(DOCUMENTS_TABLE_FETCHER.getContentUri().toString())) {
            return DOCUMENTS_TABLE_FETCHER;
        } else if (str.startsWith(APPLICATIONS_TABLE_FETCHER.getContentUri().toString())) {
            return APPLICATIONS_TABLE_FETCHER;
        } else if (str.startsWith(RINGTONES_TABLE_FETCHER.getContentUri().toString())) {
            return RINGTONES_TABLE_FETCHER;
        } else {
            return DOCUMENTS_TABLE_FETCHER;
        }
    }
}