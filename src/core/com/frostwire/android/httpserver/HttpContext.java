/*
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.frostwire.android.httpserver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HttpContext {

    private String _path;
    private String _protocol;
    private HttpHandler _handler;
    private Map<String, Object> _attributes = new HashMap<String, Object>();
    private HttpServer _server;
    /* system filters, not visible to applications */
    private LinkedList<Filter> _sfilters = new LinkedList<Filter>();
    /* user filters, set by applications */
    private LinkedList<Filter> _ufilters = new LinkedList<Filter>();

    /**
     * constructor is package private.
     */
    public HttpContext(String protocol, String path, HttpHandler handler, HttpServer server) {
        if (path == null || protocol == null || path.length() < 1 || path.charAt(0) != '/') {
            throw new IllegalArgumentException("Illegal value for path or protocol");
        }
        _protocol = protocol.toLowerCase();
        _path = path;
        if (!_protocol.equals("http")) {
            throw new IllegalArgumentException("Illegal value for protocol");
        }
        _handler = handler;
        _server = server;
    }

    /**
     * returns the handler for this context
     * @return the HttpHandler for this context
     */
    public HttpHandler getHandler() {
        return _handler;
    }

    public void setHandler(HttpHandler h) {
        if (h == null) {
            throw new NullPointerException("Null handler parameter");
        }
        if (_handler != null) {
            throw new IllegalArgumentException("handler already set");
        }
        _handler = h;
    }

    /**
     * returns the path this context was created with
     * @return this context's path
     */
    public String getPath() {
        return _path;
    }

    public HttpServer getServer() {
        return _server;
    }

    /**
     * returns the protocol this context was created with
     * @return this context's path
     */
    public String getProtocol() {
        return _protocol;
    }

    /**
     * returns a mutable Map, which can be used to pass
     * configuration and other data to Filter modules
     * and to the context's exchange handler.
     * <p>
     * Every attribute stored in this Map will be visible to
     * every HttpExchange processed by this context
     */
    public Map<String, Object> getAttributes() {
        return _attributes;
    }

    public List<Filter> getFilters() {
        return _ufilters;
    }

    public List<Filter> getSystemFilters() {
        return _sfilters;
    }
}
