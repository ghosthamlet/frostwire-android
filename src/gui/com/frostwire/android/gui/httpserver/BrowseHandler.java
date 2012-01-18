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
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import android.util.Log;

import com.frostwire.android.core.FileDescriptor;
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
class BrowseHandler implements HttpHandler {

    private static final String TAG = "FW.BrowseHandler";

    public void handle(HttpExchange exchange) throws IOException {

        GZIPOutputStream os = null;

        byte type = -1;

        try {

            List<NameValuePair> query = URLEncodedUtils.parse(exchange.getRequestURI(), "UTF-8");

            for (NameValuePair item : query) {
                if (item.getName().equals("type")) {
                    type = Byte.parseByte(item.getValue());
                }
            }

            if (type == -1) {
                exchange.sendResponseHeaders(Code.HTTP_BAD_REQUEST, 0);
                return;
            }

            String response = getResponse(exchange, type);

            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            exchange.sendResponseHeaders(Code.HTTP_OK, 0);

            os = new GZIPOutputStream(exchange.getResponseBody());

            os.write(response.getBytes());
            os.finish();

        } catch (IOException e) {
            Log.e(TAG, "Error browsing files type=" + type);
            throw e;
        } finally {
            if (os != null) {
                os.close();
            }
            exchange.close();
        }
    }

    private String getResponse(HttpExchange exchange, byte fileType) {
        List<FileDescriptor> fileDescriptors = Librarian.instance().getFiles(fileType, 0, Integer.MAX_VALUE, true);

        FileDescriptorList list = new FileDescriptorList();
        list.files = fileDescriptors;

        return JsonUtils.toJson(list);
    }

    static final class FileDescriptorList {
        public List<FileDescriptor> files;
    }
}
