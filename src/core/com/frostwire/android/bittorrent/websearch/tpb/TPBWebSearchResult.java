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

package com.frostwire.android.bittorrent.websearch.tpb;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frostwire.android.bittorrent.websearch.WebSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class TPBWebSearchResult implements WebSearchResult {

    private static final long[] BYTE_MULTIPLIERS = new long[] { 1, 2 << 9, 2 << 19, 2 << 29, 2 << 39, 2 << 49 };

    private static final Map<String, Integer> UNIT_TO_BYTE_MULTIPLIERS_MAP;
    private static final Pattern COMMON_DATE_PATTERN;
    private static final Pattern OLDER_DATE_PATTERN;
    private static final Pattern DATE_TIME_PATTERN;

    private String fileName;
    private String torrentDetailsURI;
    private String torrentURI;
    private String infoHash;
    private long size;
    private long creationTime;
    private int seeds;

    static {
        UNIT_TO_BYTE_MULTIPLIERS_MAP = new HashMap<String, Integer>();
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("B", 0);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("KiB", 1);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("MiB", 2);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("GiB", 3);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("TiB", 4);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("PiB", 5);

        COMMON_DATE_PATTERN = Pattern.compile("([\\d]{2})-([\\d]{2})");
        OLDER_DATE_PATTERN = Pattern.compile("([\\d]{2})-([\\d]{2})&nbsp;([\\d]{4})");
        DATE_TIME_PATTERN = Pattern.compile("([\\d]{2})-([\\d]{2})&nbsp;(\\d\\d:\\d\\d)");
    }

    public TPBWebSearchResult(Matcher matcher) {
        /*
         * Matcher groups cheatsheet
         * 1 -> Category (useless)
         * 2 -> Torrent Details Page
         * 3 -> Title/Name
         * 4 -> .torrent URL
         * 5 -> infoHash
         * 6 -> MM-DD&nbsp;YYYY or Today&nbsp;HH:MM or Y-day&nbsp;HH:MM 
         * 7 -> SIZE&nbsp;(B|KiB|MiBGiB)
         * 8 -> seeds
         */
        torrentDetailsURI = matcher.group(2);
        fileName = matcher.group(3);
        torrentURI = matcher.group(4);
        infoHash = matcher.group(5);
        creationTime = parseCreationTime(matcher.group(6));
        size = parseSize(matcher.group(7));
        seeds = parseSeeds(matcher.group(8));

        // fix .torrent suffix in the case of TPB
        if (!fileName.endsWith(".torrent")) {
            fileName += ".torrent";
        }
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getVendor() {
        return "TPB";
    }

    @Override
    public String getHash() {
        return infoHash;
    }

    @Override
    public String getTorrentURI() {
        return torrentURI;
    }

    @Override
    public int getSeeds() {
        return seeds;
    }

    @Override
    public String getTorrentDetailsURL() {
        return "http://thepiratebay.org" + torrentDetailsURI;
    }

    private long parseSize(String group) {
        String[] size = group.split("&nbsp;");
        String amount = size[0].trim();
        String unit = size[1].trim();

        long multiplier = BYTE_MULTIPLIERS[UNIT_TO_BYTE_MULTIPLIERS_MAP.get(unit)];

        //fractional size
        if (amount.indexOf(".") > 0) {
            float floatAmount = Float.parseFloat(amount);
            return (long) (floatAmount * multiplier);
        }
        //integer based size
        else {
            int intAmount = Integer.parseInt(amount);
            return (long) (intAmount * multiplier);
        }
    }

    private int parseSeeds(String group) {
        try {
            return Integer.parseInt(group);
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseCreationTime(String group) {
        //Today or for whatever minutes ago
        if (group.contains("Today") || group.contains("<b>")) {
            return System.currentTimeMillis();
        } else if (group.contains("Y-day")) {
            return System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        }

        Matcher OLDER_DATE_PATTERN_MATCHER = OLDER_DATE_PATTERN.matcher(group);
        Matcher COMMON_DATE_PATTERN_MATCHER = COMMON_DATE_PATTERN.matcher(group);
        Matcher DATE_TIME_PATTERN_MATCHER = DATE_TIME_PATTERN.matcher(group);

        Matcher RIGHT_MATCHER = (OLDER_DATE_PATTERN_MATCHER.matches()) ? OLDER_DATE_PATTERN_MATCHER : COMMON_DATE_PATTERN_MATCHER;

        if (!RIGHT_MATCHER.matches() && DATE_TIME_PATTERN_MATCHER.matches()) {
            RIGHT_MATCHER = DATE_TIME_PATTERN_MATCHER;
        }

        int month = Integer.parseInt(RIGHT_MATCHER.group(1));
        int date = Integer.parseInt(RIGHT_MATCHER.group(2));
        int year = 0;

        if (OLDER_DATE_PATTERN_MATCHER.matches() && OLDER_DATE_PATTERN_MATCHER.groupCount() == 3) {
            year = Integer.parseInt(RIGHT_MATCHER.group(3));
        } else if (COMMON_DATE_PATTERN_MATCHER.matches() || DATE_TIME_PATTERN_MATCHER.matches()) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }

        Calendar instance = Calendar.getInstance();
        instance.clear();
        instance.set(year, month, date);
        return instance.getTimeInMillis();
    }
}
