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

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.frostwire.android.gui.Finger;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.httpserver.Code;
import com.frostwire.android.httpserver.HttpExchange;
import com.frostwire.android.httpserver.HttpHandler;
import com.frostwire.android.util.JsonUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
class FingerHandler implements HttpHandler {

    private static final String TAG = "FW.FingerHandler";

    public void handle(HttpExchange exchange) throws IOException {

        OutputStream os = null;

        try {
            //Log.i(TAG, "Request from " + exchange.getRemoteAddress().getAddress().getHostAddress());

            String response = getResponse(exchange);

            exchange.sendResponseHeaders(Code.HTTP_OK, response.length());

            os = exchange.getResponseBody();

            os.write(response.getBytes());

        } catch (IOException e) {
            Log.e(TAG, "Error serving finger");
            throw e;
        } finally {
            if (os != null) {
                os.close();
            }
            exchange.close();
        }
    }

    private String getResponse(HttpExchange exchange) {
        Finger finger = Librarian.instance().finger(false);
        return JsonUtils.toJson(finger);
    }
}
