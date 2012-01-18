/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Headers implements Map<String, List<String>> {

    private HashMap<String, List<String>> map;

    public Headers() {
        map = new HashMap<String, List<String>>(32);
    }

    /* Normalize the key by converting to following form.
     * First char upper case, rest lower case.
     * key is presumed to be ASCII
     */
    private String normalize(String key) {
        if (key == null) {
            return null;
        }
        int len = key.length();
        if (len == 0) {
            return key;
        }
        char[] b = new char[len];
        String s = null;
        b = key.toCharArray();
        if (b[0] >= 'a' && b[0] <= 'z') {
            b[0] = (char) (b[0] - ('a' - 'A'));
        }
        for (int i = 1; i < len; i++) {
            if (b[i] >= 'A' && b[i] <= 'Z') {
                b[i] = (char) (b[i] + ('a' - 'A'));
            }
        }
        s = new String(b);
        return s;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        if (!(key instanceof String)) {
            return false;
        }
        return map.containsKey(normalize((String) key));
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public List<String> get(Object key) {
        return map.get(normalize((String) key));
    }

    /**
     * returns the first value from the List of String values
     * for the given key (if at least one exists).
     * @param key the key to search for
     * @return the first string value associated with the key
     */
    public String getFirst(String key) {
        List<String> l = map.get(normalize(key));
        if (l == null) {
            return null;
        }
        return l.get(0);
    }

    public List<String> put(String key, List<String> value) {
        return map.put(normalize(key), value);
    }

    /**
     * adds the given value to the list of headers
     * for the given key. If the mapping does not
     * already exist, then it is created
     * @param key the header name
     * @param value the header value to add to the header
     */
    public void add(String key, String value) {
        String k = normalize(key);
        List<String> l = map.get(k);
        if (l == null) {
            l = new LinkedList<String>();
            map.put(k, l);
        }
        l.add(value);
    }

    /**
     * sets the given value as the sole header value
     * for the given key. If the mapping does not
     * already exist, then it is created
     * @param key the header name
     * @param value the header value to set.
     */
    public void set(String key, String value) {
        LinkedList<String> l = new LinkedList<String>();
        l.add(value);
        put(key, l);
    }

    public List<String> remove(Object key) {
        return map.remove(normalize((String) key));
    }

    public void putAll(Map<? extends String, ? extends List<String>> t) {
        map.putAll(t);
    }

    public void clear() {
        map.clear();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<List<String>> values() {
        return map.values();
    }

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public int hashCode() {
        return map.hashCode();
    }
}
