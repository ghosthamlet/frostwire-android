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

package com.frostwire.android.gui.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.frostwire.android.util.concurrent.AbstractRunnable;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public abstract class QueueProcessor<E> extends AbstractRunnable {

    private static final String TAG = "FW.QueueProcessor";

    private final static byte STATE_PROCESSING = 0;
    private final static byte STATE_STOPPED = 1;

    private byte state = STATE_PROCESSING;

    private BlockingQueue<Object> queue;
    private ThreadPool threadPool;

    public QueueProcessor(String name, ThreadPool threadPool, int capacity) {
        super("QueueProcessor-" + name);
        this.threadPool = threadPool;
        this.queue = new LinkedBlockingQueue<Object>(capacity);

        this.state = STATE_STOPPED;
    }

    public void addElement(E element) {
        if (state != STATE_PROCESSING) {
            return;
        }

        queue.offer(element);
    }

    public void startProcessing() {
        state = STATE_PROCESSING;
        threadPool.execute(this);
    }

    public void run() {
        try {

            onStartProcessing();
            processQueue();

        } catch (Throwable e) {
            Log.e(TAG, "General failure in queue processor: " + getName(), e);
        }
    }

    public synchronized void stopProcessing() {
        state = STATE_STOPPED;

        onStopProcessing();
        queue.clear();
        queue.offer(new End());
    }

    public boolean isProcessing() {
        return state == STATE_PROCESSING;
    }

    public boolean isStopped() {
        return state == STATE_STOPPED;
    }

    public abstract void processElement(E elem) throws Exception;

    protected ThreadPool getThreadPool() {
        return threadPool;
    }

    protected void onStartProcessing() {
    }

    protected void onStopProcessing() {
    }

    @SuppressWarnings("unchecked")
    private void processQueue() throws Exception {
        E element = null;

        while (isProcessing()) {
            element = (E) queue.take(); //this will wait if queue is empty

            if (element instanceof End) {
                return;
            }

            if (element != null) {
                try {
                    processElement(element);
                } catch (Throwable e) {
                    Log.e(TAG, "Error processing element " + element + ", error=" + e.getMessage(), e);
                }
            }
        }
    }

    private static final class End {
    }
}
