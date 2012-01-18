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

package com.frostwire.android.util.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class ThreadPool extends ThreadPoolExecutor {

    private String name;

    public ThreadPool(String name) {
        super(0, Integer.MAX_VALUE, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        this.name = name;
    }

    public ThreadPool(String name, int maxConcurrent) {
        super(maxConcurrent, maxConcurrent, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (r instanceof ExtendedRunnable) {
            ExtendedRunnable er = (ExtendedRunnable) r;
            t.setName(name + "::" + er.getName());
        }
        super.beforeExecute(t, r);
    }
}
