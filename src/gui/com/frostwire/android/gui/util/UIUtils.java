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

package com.frostwire.android.gui.util;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.core.MediaType;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.gui.services.Engine;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class UIUtils {

    private static final String TAG = "FW.UIUtils";

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss aa");

    /**
     * Localizable Number Format constant for the current default locale.
     */
    private static NumberFormat NUMBER_FORMAT0; // localized "#,##0"
    private static NumberFormat NUMBER_FORMAT1; // localized "#,##0.0"

    private static String[] BYTE_UNITS = new String[] { "b", "KB", "Mb", "Gb", "Tb" };

    public static String GENERAL_UNIT_KBPSEC = "KB/s";

    static {
        NUMBER_FORMAT0 = NumberFormat.getNumberInstance(Locale.getDefault());
        NUMBER_FORMAT0.setMaximumFractionDigits(0);
        NUMBER_FORMAT0.setMinimumFractionDigits(0);
        NUMBER_FORMAT0.setGroupingUsed(true);

        NUMBER_FORMAT1 = NumberFormat.getNumberInstance(Locale.getDefault());
        NUMBER_FORMAT1.setMaximumFractionDigits(1);
        NUMBER_FORMAT1.setMinimumFractionDigits(1);
        NUMBER_FORMAT1.setGroupingUsed(true);
    }

    public static void showToastMessage(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public static void showShortMessage(Context context, String message) {
        showToastMessage(context, message, Toast.LENGTH_SHORT);
    }

    public static void showLongMessage(Context context, String message) {
        showToastMessage(context, message, Toast.LENGTH_LONG);
    }

    public static void showShortMessage(Context context, int resId) {
        showShortMessage(context, context.getString(resId));
    }

    public static void showLongMessage(Context context, int resId) {
        showLongMessage(context, context.getString(resId));
    }

    public static void showShortMessage(Context context, int resId, Object... formatArgs) {
        showShortMessage(context, context.getString(resId, formatArgs));
    }

    /**
     * 
     * @param context
     * @param messageId
     * @param titleId
     * @param positiveListener
     */
    public static Dialog showYesNoDialog(Context context, int messageId, int titleId, OnClickListener positiveListener) {
        return showYesNoDialog(context, messageId, titleId, positiveListener, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public static void showYesNoDialog(Context context, int iconId, int messageId, int titleId, OnClickListener positiveListener) {
        showYesNoDialog(context, iconId, messageId, titleId, positiveListener, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public static void showYesNoDialog(Context context, int iconId, String message, int titleId, OnClickListener positiveListener) {
        showYesNoDialog(context, iconId, message, titleId, positiveListener, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 
     * @param context
     * @param messageId
     * @param titleId
     * @param positiveListener
     * @param negativeListener
     */
    public static Dialog showYesNoDialog(Context context, int messageId, int titleId, OnClickListener positiveListener, OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageId).setTitle(titleId).setCancelable(false).setPositiveButton(android.R.string.yes, positiveListener).setNegativeButton(android.R.string.no, negativeListener);
        Dialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static void showYesNoDialog(Context context, int iconId, int messageId, int titleId, OnClickListener positiveListener, OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setIcon(iconId).setMessage(messageId).setTitle(titleId).setCancelable(false).setPositiveButton(android.R.string.yes, positiveListener).setNegativeButton(android.R.string.no, negativeListener);

        builder.create().show();
    }

    public static void showYesNoDialog(Context context, String message, int titleId, OnClickListener positiveListener, OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message).setTitle(titleId).setCancelable(false).setPositiveButton(android.R.string.yes, positiveListener).setNegativeButton(android.R.string.no, negativeListener);

        builder.create().show();
    }

    public static void showYesNoDialog(Context context, int iconId, String message, int titleId, OnClickListener positiveListener, OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setIcon(iconId).setMessage(message).setTitle(titleId).setCancelable(false).setPositiveButton(android.R.string.yes, positiveListener).setNegativeButton(android.R.string.no, negativeListener);

        builder.create().show();
    }

    public static void showOkCancelDialog(Context context, View view, int titleId, OnClickListener okListener, OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(view).setTitle(titleId).setPositiveButton(android.R.string.ok, okListener).setNegativeButton(android.R.string.cancel, cancelListener);

        builder.create().show();
    }

    public static void showOkCancelDialog(Context context, View view, int titleId, OnClickListener okListener) {
        showOkCancelDialog(context, view, titleId, okListener, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public static EditText buildEditTextWithType(Context context, KeyListener keyListener, String text) {
        EditText editText = new EditText(context);
        editText.setKeyListener(keyListener);
        editText.setText(text);
        return editText;
    }

    public static EditText buildNumericEditText(Context context, String text) {
        return buildEditTextWithType(context, new NumberKeyListener() {
            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }

            @Override
            protected char[] getAcceptedChars() {
                return new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
            }
        }, text);
    }

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String getBytesInHuman(float size) {
        int i = 0;
        for (i = 0; size > 1024; i++) {
            size /= 1024f;
        }
        return String.format("%.2f %s", size, BYTE_UNITS[i]);
    }

    public static String getBytesInHuman(double size) {
        int i = 0;
        for (i = 0; size > 1024; i++) {
            size /= 1024f;
        }
        return String.format("%.2f %s", size, BYTE_UNITS[i]);
    }

    /**
     * Converts an rate into a human readable and localized KB/s speed.
     */
    public static String rate2speed(double rate) {
        return NUMBER_FORMAT0.format(rate) + " " + GENERAL_UNIT_KBPSEC;
    }

    /**
     * Retrieve the resource id for the file type icon.
     * 
     * @param fileType The desired file type from GlobalConstants.
     * @return The icon resource id.
     */
    public static int getFileTypeIconId(byte fileType) {
        switch (fileType) {
        case Constants.FILE_TYPE_APPLICATIONS:
            return R.drawable.application_selector_off;
        case Constants.FILE_TYPE_AUDIO:
            return R.drawable.audio_selector_off;
        case Constants.FILE_TYPE_DOCUMENTS:
            return R.drawable.document_selector_off;
        case Constants.FILE_TYPE_PICTURES:
            return R.drawable.picture_selector_off;
        case Constants.FILE_TYPE_RINGTONES:
            return R.drawable.ringtone_selector_off;
        case Constants.FILE_TYPE_VIDEOS:
            return R.drawable.video_selector_off;
        default:
            return R.drawable.question_mark;
        }
    }

    public static String getFileTypeAsString(Resources resources, byte fileType) {
        switch (fileType) {
        case Constants.FILE_TYPE_APPLICATIONS:
            return resources.getString(R.string.applications);
        case Constants.FILE_TYPE_AUDIO:
            return resources.getString(R.string.audio);
        case Constants.FILE_TYPE_DOCUMENTS:
            return resources.getString(R.string.documents);
        case Constants.FILE_TYPE_PICTURES:
            return resources.getString(R.string.pictures);
        case Constants.FILE_TYPE_RINGTONES:
            return resources.getString(R.string.ringtones);
        case Constants.FILE_TYPE_VIDEOS:
            return resources.getString(R.string.video);
        default:
            return "Unkown file type";
        }
    }

    /**
     * Opens the given file with the default Android activity for that File and
     * mime type.
     * 
     * @param filePath
     * @param mime
     */
    public static void openFile(Context context, String filePath, String mime) {
        try {
            if (!openAudioInternal(filePath)) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(filePath)), mime);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        } catch (Throwable e) {
            UIUtils.showShortMessage(context, R.string.cant_open_file);
            Log.e(TAG, "Failed to open file: " + filePath);
        }
    }

    public static void openFile(Context context, File file) {
        openFile(context, file.getAbsolutePath(), getMimeType(file.getAbsolutePath()));
    }

    public static int getFileTypeIconId(String ext) {
        MediaType mt = MediaType.getMediaTypeForExtension(ext);
        if (mt == null) {
            return R.drawable.question_mark;
        }
        if (mt.equals(MediaType.getApplicationsMediaType())) {
            return R.drawable.application;
        } else if (mt.equals(MediaType.getAudioMediaType())) {
            return R.drawable.audio;
        } else if (mt.equals(MediaType.getDocumentMediaType())) {
            return R.drawable.document;
        } else if (mt.equals(MediaType.getImageMediaType())) {
            return R.drawable.picture;
        } else if (mt.equals(MediaType.getVideoMediaType())) {
            return R.drawable.video;
        } else if (mt.equals(MediaType.getTorrentMediaType())) {
            return R.drawable.torrent;
        } else {
            return R.drawable.question_mark;
        }
    }

    public static String getMimeType(String filePath) {
        try {
            URL u = new URL("file://" + filePath);
            URLConnection uc = null;
            uc = u.openConnection();
            return uc.getContentType();
        } catch (Throwable e) {
            Log.e(TAG, "Failed to read mime type for: " + filePath);
            return "";
        }
    }

    private static boolean openAudioInternal(String filePath) {
        try {
            String where = MediaColumns.DATA + " LIKE ?";
            String[] whereArgs = new String[] { filePath };

            List<FileDescriptor> fds = Librarian.instance().getFiles(Constants.FILE_TYPE_AUDIO, where, whereArgs);

            if (fds.size() == 1) {
                Engine.instance().playMedia(fds.get(0));
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            return false;
        }
    }
}
