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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ExecutorsHelper {

    public static ExecutorService newFixedSizeThreadPool(int size, String name) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(size, size, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), daemonThreadFactory(name));
        return Executors.unconfigurableExecutorService(executor);
    }

    public static ExecutorService newFixedSizePriorityThreadPool(int size, String name) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(size, size, 5L, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(), daemonThreadFactory(name));
        return Executors.unconfigurableExecutorService(executor);
    }

    private static ThreadFactory daemonThreadFactory(String name) {
        return new DefaultThreadFactory(name, true);
    }
}
