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

package com.frostwire.android.gui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ImageView;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.FileDescriptor;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.gui.util.MusicUtils;
import com.frostwire.android.gui.views.AbstractActivity;
import com.frostwire.android.gui.views.MediaControllerView;
import com.frostwire.android.util.StringUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class MediaPlayerActivity extends AbstractActivity implements MediaControllerView.MediaPlayerControl {

    private static final String TAG = "FW.MediaPlayerActivity";
    
    private static final String POLAROID_TAG_FONT = "fonts/aescrawl.ttf";

    private MediaControllerView mediaController;
    private MediaPlayer mediaPlayer;
    private FileDescriptor mediaFD;

    private BroadcastReceiver broadcastReceiver;

    public MediaPlayerActivity() {
        super(R.layout.activity_media_player);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.ACTION_MEDIA_PLAYER_STOPPED)) {
                    try {
                        finish();
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }
        };
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mediaController != null) {
            mediaController.sync();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            finish();
            Engine.instance().stopMedia();
        }
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (Throwable e) {
                // ignore
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (Throwable e) {
                // ignore
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void seekTo(int i) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(i);
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.isPlaying();
            } catch (Throwable e) {
                // ignore
                return false;
            }
        } else {
            return false;
        }
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canStop() {
        return true;
    }

    @Override
    protected void initComponents() {
        
        mediaPlayer = Engine.instance().getMediaPlayer();
        mediaFD = Engine.instance().getMediaFD();
        
        setTitle(getString(R.string.application_label) + ": " + mediaFD.artist + " - " + mediaFD.title);

        if (mediaPlayer != null) {
            mediaController = findView(R.id.activity_media_player_media_controller);
            mediaController.setMediaPlayer(this);
        }

        if (mediaFD != null) {
            ImageView image = findView(R.id.activity_media_player_image);
            Bitmap coverArt = readCoverArt();
            if (coverArt != null) {
                image.setImageBitmap(coverArt);
            }
        } else {
            Engine.instance().stopMedia();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(Constants.ACTION_MEDIA_PLAYER_STOPPED);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(broadcastReceiver);
    }

    private Bitmap readCoverArt() {
        Bitmap artwork = null;

        try {
            artwork = MusicUtils.getArtwork(this, mediaFD.id, -1);
            artwork = applyEffect(artwork);
        } catch (Throwable e) {
            Log.e(TAG, "Can't read the cover art for fd: " + mediaFD);
        }

        return artwork;
    }

    /**
     * For now, the "simple" Polaroid frame effect
     * @param bmp
     * @return
     */
    private Bitmap applyEffect(Bitmap bmp) {
        int hDelta = 64;
        hDelta += StringUtils.isNullOrEmpty(mediaFD.artist, true) ? 0 : 64;
        hDelta += StringUtils.isNullOrEmpty(mediaFD.title, true) ? 0 : 64;

        Bitmap b = Bitmap.createBitmap(bmp.getWidth() + 64, bmp.getHeight() + hDelta, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p;

        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        p.setColor(Color.TRANSPARENT);
        c.drawRect(0, 0, b.getWidth(), b.getHeight(), p);

        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        p.setMaskFilter(new BlurMaskFilter(8, Blur.OUTER));
        c.drawRect(10, 12, b.getWidth() - 10, b.getHeight() - 8, p);

        //        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        //        p.setColor(Color.TRANSPARENT);
        //        c.drawRect(0, 0, b.getWidth(), 4, p);
        //
        //        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        //        p.setColor(Color.TRANSPARENT);
        //        c.drawRect(0, 0, 2, b.getHeight(), p);
        //
        //        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        //        p.setColor(Color.TRANSPARENT);
        //        c.drawRect(b.getHeight() - 2, 0, b.getWidth(), b.getHeight(), p);

        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        c.drawRect(8, 8, b.getWidth() - 8, b.getHeight() - 8, p);

        //p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //p.setColor(Color.BLACK);
        //p.setMaskFilter(new BlurMaskFilter(6, Blur.OUTER));
        //c.drawRect(32, 32, b.getWidth() - 32, b.getHeight() - 32, p);

        //p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //p.setColor(Color.WHITE);
        //c.drawRect(16, 16, b.getWidth() - 16, 32, p);

        //p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //p.setColor(Color.WHITE);
        //c.drawRect(16, 16, 32, b.getHeight() - 16, p);

        p = new Paint(Paint.DITHER_FLAG);
        c.drawBitmap(bmp, 32, 32, p);

        if (mediaFD.artist != null) {
            p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setTypeface(Typeface.createFromAsset(getAssets(), POLAROID_TAG_FONT));
            p.setTextSize(42);
            p.setARGB(255, 63, 63, 255);
            c.drawText(mediaFD.artist, 38, b.getHeight() - 114, p);
        }

        if (mediaFD.title != null) {
            p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setTypeface(Typeface.createFromAsset(getAssets(), POLAROID_TAG_FONT));
            p.setTextSize(38);
            p.setARGB(255, 63, 63, 255);
            c.drawText(mediaFD.title, 38, b.getHeight() - 52, p);
        }

        return b;
    }
}
