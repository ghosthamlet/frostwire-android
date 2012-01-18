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

package com.frostwire.android.gui.httpserver;

import java.net.InetSocketAddress;

import android.util.Log;

import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.httpserver.HttpServer;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class HttpServerManager {

    private static final String TAG = "FW.HttpServerManager";

    private ThreadPool threadPool;
    private HttpServer httpServer;

    public HttpServerManager(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void start(int port) {
        if (!NetworkManager.instance().isDataWIFIUp()) {
            return;
        }

        if (httpServer != null) {
            return;
        }

        try {

            httpServer = new HttpServer("http", new InetSocketAddress(port), 10);

            httpServer.createContext("/finger", new FingerHandler());
            httpServer.createContext("/browse", new BrowseHandler());
            httpServer.createContext("/download", new DownloadHandler());

            httpServer.setExecutor(threadPool);
            httpServer.start();

        } catch (Throwable e) {
            Log.e(TAG, "Failed to start http server", e);
        }
    }

    public void stop() {
        if (httpServer == null) {
            return;
        }

        try {
            httpServer.stop(0);
        } catch (Throwable e) {
            Log.e(TAG, "Something wrong stopping the HTTP server", e);
        } finally {
            httpServer = null;
        }
    }
}
