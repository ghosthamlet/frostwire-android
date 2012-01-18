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

import java.util.Formatter;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.frostwire.android.R;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class MediaControllerView extends FrameLayout {

    private static final int SHOW_PROGRESS = 1;

    private MediaPlayerControl player;
    private ImageButton buttonPause;
    private ImageButton buttonStop;
    private ProgressBar progress;
    private TextView endTime;
    private TextView currentTime;

    private boolean dragging;

    private StringBuilder formatBuilder;
    private Formatter formatter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
            case SHOW_PROGRESS:
                pos = setProgress();
                if (!dragging && player.isPlaying()) {
                    msg = obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg, 1000 - (pos % 1000));
                }
                break;
            }
        }
    };

    private View.OnClickListener pauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            sync();
        }
    };

    private View.OnClickListener stopListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (player != null) {
                player.stop();
                setProgress();
                sync();
            }
        }
    };

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            sync();

            dragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            handler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = player.getDuration();
            long newposition = (duration * progress) / 1000L;
            player.seekTo((int) newposition);
            if (currentTime != null)
                currentTime.setText(stringForTime((int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            dragging = false;
            setProgress();
            updatePausePlay();
            sync();

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            handler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    public MediaControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_media_controller, this);

        buttonPause = (ImageButton) findViewById(R.id.view_media_controller_pause);
        if (buttonPause != null) {
            buttonPause.requestFocus();
            buttonPause.setOnClickListener(pauseListener);
        }

        buttonStop = (ImageButton) findViewById(R.id.view_media_controller_stop);
        if (buttonStop != null) {
            buttonStop.setOnClickListener(stopListener);
        }

        progress = (ProgressBar) findViewById(R.id.view_media_controller_progress);
        if (progress != null) {
            if (progress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) progress;
                seeker.setOnSeekBarChangeListener(seekListener);
            }
            progress.setMax(1000);
        }

        endTime = (TextView) findViewById(R.id.view_media_controller_time_end);
        currentTime = (TextView) findViewById(R.id.view_media_controller_time_current);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        this.player = player;
        updatePausePlay();
    }

    public void sync() {
        setProgress();
        if (buttonPause != null) {
            buttonPause.requestFocus();
        }
        disableUnsupportedButtons();
        updatePausePlay();

        // cause the progress bar to be updated This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        handler.sendEmptyMessage(SHOW_PROGRESS);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                sync();
                if (buttonPause != null) {
                    buttonPause.requestFocus();
                }
            }
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (uniqueDown && player.isPlaying()) {
                updatePausePlay();
                sync();
                player.stop();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        }

        sync();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (buttonPause != null) {
            buttonPause.setEnabled(enabled);
        }
        if (buttonStop != null) {
            buttonStop.setEnabled(enabled);
        }
        if (progress != null) {
            progress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private void disableUnsupportedButtons() {
        try {
            if (buttonPause != null && !player.canPause()) {
                buttonPause.setEnabled(false);
            }
        } catch (Throwable e) {
        }
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (player == null || dragging) {
            return 0;
        }
        int position = player.getCurrentPosition();
        int duration = player.getDuration();
        if (progress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                progress.setProgress((int) pos);
            }
            int percent = player.getBufferPercentage();
            progress.setSecondaryProgress(percent * 10);
        }

        if (endTime != null) {
            endTime.setText(stringForTime(duration));
        }
        if (currentTime != null) {
            currentTime.setText(stringForTime(position));
        }

        return position;
    }

    private void updatePausePlay() {
        if (buttonPause == null || player == null) {
            return;
        }

        if (player.isPlaying()) {
            buttonPause.setImageResource(R.drawable.pause);
        } else {
            buttonPause.setImageResource(R.drawable.play);
        }
    }

    private void doPauseResume() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.resume();
        }
        updatePausePlay();
    }

    public static interface MediaPlayerControl {
        void pause();

        void resume();

        void stop();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canStop();
    }
}
