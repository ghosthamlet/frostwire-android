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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import android.util.Log;

/**
 * Provides implementation for HTTP
 */
public class HttpServer {

    private static final String TAG = "FW.HttpServer";

    private static final int CLOCK_TICK = ServerConfig.getClockTick();
    private static final long IDLE_INTERVAL = ServerConfig.getIdleInterval();
    private static final int MAX_IDLE_CONNECTIONS = ServerConfig.getMaxIdleConnections();

    private String _protocol;
    private Executor _executor;
    private ContextList _contexts;
    private ServerSocketChannel _schan;
    private Selector _selector;
    private SelectionKey _listenerKey;
    private Set<HttpConnection> _idleConnections;
    private Set<HttpConnection> _allConnections;
    private List<Event> _events;

    private Object _lolock = new Object();

    private volatile boolean _finished = false;
    private volatile boolean _terminating = false;

    private boolean _bound = false;
    private boolean _started = false;

    private volatile long _time; /* current time */
    //private volatile long _ticks; /* number of clock ticks since server started */

    private Timer _timer;

    public HttpServer(String protocol, InetSocketAddress addr, int backlog) throws IOException {

        _protocol = protocol;
        _contexts = new ContextList();
        _schan = ServerSocketChannel.open();
        if (addr != null) {
            ServerSocket socket = _schan.socket();
            socket.bind(addr, backlog);
            _bound = true;
        }
        _selector = Selector.open();
        _schan.configureBlocking(false);
        _listenerKey = _schan.register(_selector, SelectionKey.OP_ACCEPT);
        dispatcher = new Dispatcher();
        _idleConnections = Collections.synchronizedSet(new HashSet<HttpConnection>());
        _allConnections = Collections.synchronizedSet(new HashSet<HttpConnection>());
        _time = System.currentTimeMillis();
        _timer = new Timer("server-timer", true);
        _timer.schedule(new ServerTimerTask(), CLOCK_TICK, CLOCK_TICK);
        _events = new LinkedList<Event>();
        //Log.d(TAG, "HttpServer created " + protocol + " " + addr);
    }

    public void bind(InetSocketAddress addr, int backlog) throws IOException {
        if (_bound) {
            throw new BindException("HttpServer already bound");
        }
        if (addr == null) {
            throw new NullPointerException("null address");
        }
        ServerSocket socket = _schan.socket();
        socket.bind(addr, backlog);
        _bound = true;
    }

    public void start() {
        if (!_bound || _started || _finished) {
            throw new IllegalStateException("server in wrong state");
        }
        if (_executor == null) {
            _executor = new DefaultExecutor();
        }
        Thread t = new Thread(dispatcher);
        _started = true;
        t.start();
    }

    public void setExecutor(Executor executor) {
        if (_started) {
            throw new IllegalStateException("server already started");
        }
        this._executor = executor;
    }

    private static class DefaultExecutor implements Executor {
        public void execute(Runnable task) {
            task.run();
        }
    }

    public Executor getExecutor() {
        return _executor;
    }

    public void stop(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("negative delay parameter");
        }
        _terminating = true;
        try {
            _schan.close();
        } catch (IOException e) {
        }
        try {
            _selector.wakeup();
        } catch (Exception e) {
        }
        long latest = System.currentTimeMillis() + delay * 1000;
        while (System.currentTimeMillis() < latest) {
            delay();
            if (_finished) {
                break;
            }
        }
        _finished = true;
        try {
            _selector.wakeup();
        } catch (Exception e) {
        }
        synchronized (_allConnections) {
            for (HttpConnection c : _allConnections) {
                c.close();
            }
        }
        _allConnections.clear();
        _idleConnections.clear();
        _timer.cancel();
    }

    Dispatcher dispatcher;

    public synchronized HttpContext createContext(String path, HttpHandler handler) {
        if (handler == null || path == null) {
            throw new NullPointerException("null handler, or path parameter");
        }
        HttpContext context = new HttpContext(_protocol, path, handler, this);
        _contexts.add(context);
        //Log.d(TAG, "context created: " + path);
        return context;
    }

    public synchronized HttpContext createContext(String path) {
        if (path == null) {
            throw new NullPointerException("null path parameter");
        }
        HttpContext context = new HttpContext(_protocol, path, null, this);
        _contexts.add(context);
        //Log.d(TAG, "context created: " + path);
        return context;
    }

    public synchronized void removeContext(String path) throws IllegalArgumentException {
        if (path == null) {
            throw new NullPointerException("null path parameter");
        }
        _contexts.remove(_protocol, path);
        //Log.d(TAG, "context removed: " + path);
    }

    public synchronized void removeContext(HttpContext context) throws IllegalArgumentException {
        _contexts.remove(context);
        //Log.d(TAG, "context removed: " + context.getPath());
    }

    public InetSocketAddress getAddress() {
        return (InetSocketAddress) _schan.socket().getLocalSocketAddress();
    }

    public void addEvent(Event r) {
        synchronized (_lolock) {
            _events.add(r);
            _selector.wakeup();
        }
    }

    private int resultSize() {
        synchronized (_lolock) {
            return _events.size();
        }
    }

    /* main server listener task */
    private final class Dispatcher implements Runnable {

        private void handleEvent(Event r) {
            HttpExchange t = r.exchange;
            HttpConnection c = t.getConnection();
            try {
                if (r instanceof WriteFinishedEvent) {

                    int exchanges = endExchange();
                    if (_terminating && exchanges == 0) {
                        _finished = true;
                    }
                    LeftOverInputStream is = t.getOriginalInputStream();
                    if (!is.isEOF()) {
                        t.close = true;
                    }
                    if (t.close || _idleConnections.size() >= MAX_IDLE_CONNECTIONS) {
                        c.close();
                        _allConnections.remove(c);
                    } else {
                        if (is.isDataBuffered()) {
                            /* don't re-enable the interestops, just handle it */
                            handle(c.getChannel(), c);
                        } else {
                            /* re-enable interestops */
                            SelectionKey key = c.getSelectionKey();
                            if (key.isValid()) {
                                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                            }
                            c.time = getTime() + IDLE_INTERVAL;
                            _idleConnections.add(c);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Dispatcher (1)", e);
                c.close();
            }
        }

        public void run() {
            while (!_finished) {
                try {

                    /* process the events list first */

                    while (resultSize() > 0) {
                        Event r;
                        synchronized (_lolock) {
                            r = _events.remove(0);
                            handleEvent(r);
                        }
                    }

                    _selector.select(1000);

                    /* process the selected list now  */

                    Set<SelectionKey> selected = _selector.selectedKeys();
                    Iterator<SelectionKey> iter = selected.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.equals(_listenerKey)) {
                            if (_terminating) {
                                continue;
                            }
                            SocketChannel chan = _schan.accept();
                            if (chan == null) {
                                continue; /* cancel something ? */
                            }
                            chan.configureBlocking(false);
                            SelectionKey newkey = chan.register(_selector, SelectionKey.OP_READ);
                            HttpConnection c = new HttpConnection();
                            c.selectionKey = newkey;
                            c.setChannel(chan);
                            newkey.attach(c);
                            _allConnections.add(c);
                        } else {
                            try {
                                if (key.isReadable()) {
                                    SocketChannel chan = (SocketChannel) key.channel();
                                    HttpConnection conn = (HttpConnection) key.attachment();
                                    // interestOps will be restored at end of read
                                    key.interestOps(0);
                                    handle(chan, conn);
                                } else {
                                    assert false;
                                }
                            } catch (IOException e) {
                                HttpConnection conn = (HttpConnection) key.attachment();
                                Log.e(TAG, "Dispatcher (2)", e);
                                conn.close();
                            }
                        }
                    }
                } catch (CancelledKeyException e) {
                    Log.e(TAG, "Dispatcher (3)", e);
                } catch (IOException e) {
                    Log.e(TAG, "Dispatcher (4)", e);
                } catch (Exception e) {
                    Log.e(TAG, "Dispatcher (7)", e);
                }
            }
        }

        public void handle(SocketChannel chan, HttpConnection conn) throws IOException {
            try {
                Exchange t = new Exchange(chan, _protocol, conn);
                _executor.execute(t);
            } catch (HttpError e1) {
                Log.e(TAG, "Dispatcher (5)", e1);
                conn.close();
            } catch (IOException e) {
                Log.e(TAG, "Dispatcher (6)", e);
                conn.close();
            }
        }
    }

    static boolean debug = ServerConfig.debugEnabled();

    static synchronized void dprint(String s) {
        if (debug) {
            Log.d(TAG, s);
        }
    }

    static synchronized void dprint(Exception e) {
        if (debug) {
            Log.d(TAG, e.getMessage(), e);
        }
    }

    /* per exchange task */
    private class Exchange implements Runnable {

        private SocketChannel _channel;
        private HttpConnection _connection;
        private HttpContext _context;
        private InputStream _rawin;
        private OutputStream _rawout;
        private String protocol;
        private HttpExchange _tx;
        private HttpContext _ctx;

        public Exchange(SocketChannel chan, String protocol, HttpConnection conn) throws IOException {
            _channel = chan;
            _connection = conn;
            this.protocol = protocol;
        }

        public void run() {
            /* context will be null for new connections */
            _context = _connection.getHttpContext();
            boolean newconnection;
            String requestLine = null;
            try {
                if (_context != null) {
                    _rawin = _connection.getInputStream();
                    _rawout = _connection.getRawOutputStream();
                    newconnection = false;
                } else {
                    /* figure out what kind of connection this is */
                    newconnection = true;
                    _rawin = new BufferedInputStream(new Request.ReadStream(HttpServer.this, _channel));
                    _rawout = new Request.WriteStream(HttpServer.this, _channel);
                    _connection.raw = _rawin;
                    _connection.rawout = _rawout;
                }
                Request req = new Request(_rawin, _rawout);
                requestLine = req.requestLine();
                if (requestLine == null) {
                    /* connection closed */
                    _connection.close();
                    return;
                }
                int space = requestLine.indexOf(' ');
                if (space == -1) {
                    reject(Code.HTTP_BAD_REQUEST, requestLine, "Bad request line");
                    return;
                }
                String method = requestLine.substring(0, space);
                int start = space + 1;
                space = requestLine.indexOf(' ', start);
                if (space == -1) {
                    reject(Code.HTTP_BAD_REQUEST, requestLine, "Bad request line");
                    return;
                }
                String uriStr = requestLine.substring(start, space);
                URI uri = new URI(uriStr);
                start = space + 1;
                String version = requestLine.substring(start);
                Headers headers = req.headers();
                String s = headers.getFirst("Transfer-encoding");
                int clen = 0;
                if (s != null && s.equalsIgnoreCase("chunked")) {
                    clen = -1;
                } else {
                    s = headers.getFirst("Content-Length");
                    if (s != null) {
                        clen = Integer.parseInt(s);
                    }
                }
                _ctx = _contexts.findContext(protocol, uri.getPath());
                if (_ctx == null) {
                    reject(Code.HTTP_NOT_FOUND, requestLine, "No context found for request");
                    return;
                }
                _connection.setContext(_ctx);
                if (_ctx.getHandler() == null) {
                    reject(Code.HTTP_INTERNAL_ERROR, requestLine, "No handler for context");
                    return;
                }
                _tx = new HttpExchange(method, uri, req, clen, _connection);
                String chdr = headers.getFirst("Connection");
                Headers rheaders = _tx.getResponseHeaders();

                if (chdr != null && chdr.equalsIgnoreCase("close")) {
                    _tx.close = true;
                }
                if (version.equalsIgnoreCase("http/1.0")) {
                    _tx.http10 = true;
                    if (chdr == null) {
                        _tx.close = true;
                        rheaders.set("Connection", "close");
                    } else if (chdr.equalsIgnoreCase("keep-alive")) {
                        rheaders.set("Connection", "keep-alive");
                        int idle = (int) ServerConfig.getIdleInterval() / 1000;
                        int max = ServerConfig.getMaxIdleConnections();
                        String val = "timeout=" + idle + ", max=" + max;
                        rheaders.set("Keep-Alive", val);
                    }
                }

                if (newconnection) {
                    _connection.setParameters(_rawin, _rawout, _channel, protocol, _ctx, _rawin);
                }
                /* check if client sent an Expect 100 Continue.
                 * In that case, need to send an interim response.
                 * In future API may be modified to allow app to
                 * be involved in this process.
                 */
                String exp = headers.getFirst("Expect");
                if (exp != null && exp.equalsIgnoreCase("100-continue")) {
                    logReply(100, requestLine, null);
                    sendReply(Code.HTTP_CONTINUE, false, null);
                }
                /* uf is the list of filters seen/set by the user.
                 * sf is the list of filters established internally
                 * and which are not visible to the user. uc and sc
                 * are the corresponding Filter.Chains.
                 * They are linked together by a LinkHandler
                 * so that they can both be invoked in one call.
                 */
                List<Filter> sf = _ctx.getSystemFilters();
                List<Filter> uf = _ctx.getFilters();

                Filter.Chain sc = new Filter.Chain(sf, _ctx.getHandler());
                Filter.Chain uc = new Filter.Chain(uf, new LinkHandler(sc));

                /* set up the two stream references */
                _tx.getRequestBody();
                _tx.getResponseBody();
                uc.doFilter(_tx);

            } catch (IOException e1) {
                Log.e(TAG, "ServerImpl.Exchange (1), e: " + e1.getMessage());
                _connection.close();
            } catch (NumberFormatException e3) {
                reject(Code.HTTP_BAD_REQUEST, requestLine, "NumberFormatException thrown");
            } catch (URISyntaxException e) {
                reject(Code.HTTP_BAD_REQUEST, requestLine, "URISyntaxException thrown");
            } catch (Throwable e4) {
                Log.e(TAG, "ServerImpl.Exchange (2)", e4);
                _connection.close();
            }
        }

        /* used to link to 2 or more Filter.Chains together */

        private class LinkHandler implements HttpHandler {
            Filter.Chain nextChain;

            LinkHandler(Filter.Chain nextChain) {
                this.nextChain = nextChain;
            }

            public void handle(HttpExchange exchange) throws IOException {
                nextChain.doFilter(exchange);
            }
        }

        void reject(int code, String requestStr, String message) {
            logReply(code, requestStr, message);
            sendReply(code, true, "<h1>" + code + Code.msg(code) + "</h1>" + message);
        }

        void sendReply(int code, boolean closeNow, String text) {
            try {
                String s = "HTTP/1.1 " + code + Code.msg(code) + "\r\n";
                if (text != null && text.length() != 0) {
                    s = s + "Content-Length: " + text.length() + "\r\n";
                    s = s + "Content-Type: text/html\r\n";
                } else {
                    s = s + "Content-Length: 0\r\n";
                    text = "";
                }
                if (closeNow) {
                    s = s + "Connection: close\r\n";
                }
                s = s + "\r\n" + text;
                byte[] b = s.getBytes("ISO8859_1");
                _rawout.write(b);
                _rawout.flush();
                if (closeNow) {
                    _connection.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "ServerImpl.sendReply", e);
                _connection.close();
            }
        }

    }

    public void logReply(int code, String requestStr, String text) {
        if (text == null) {
            text = "";
        }
        String message = requestStr + " [" + code + " " + Code.msg(code) + "] (" + text + ")";
        Log.i(TAG, message);
    }

    public long getTime() {
        return _time;
    }

    private void delay() {
        Thread.yield();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
    }

    private int exchangeCount = 0;

    public synchronized void startExchange() {
        exchangeCount++;
    }

    private synchronized int endExchange() {
        exchangeCount--;
        assert exchangeCount >= 0;
        return exchangeCount;
    }

    /**
     * TimerTask run every CLOCK_TICK ms
     */
    private class ServerTimerTask extends TimerTask {
        public void run() {
            LinkedList<HttpConnection> toClose = new LinkedList<HttpConnection>();
            _time = System.currentTimeMillis();
            //_ticks++;
            synchronized (_idleConnections) {
                for (HttpConnection c : _idleConnections) {
                    if (c.time <= _time) {
                        toClose.add(c);
                    }
                }
                for (HttpConnection c : toClose) {
                    _idleConnections.remove(c);
                    _allConnections.remove(c);
                    c.close();
                }
            }
        }
    }
}
