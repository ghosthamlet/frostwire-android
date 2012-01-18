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

package com.frostwire.android.util;

import java.util.Random;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class ByteUtils {

    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private ByteUtils() {
    }

    // 2 BYTE ARRAY (65535 bytes)

    /**
     * Given an int, returns the int as a 2 byte array of unsigned bytes.
     */
    public static byte[] smallIntToByteArray(int v) {
        if (v > 65535) {
            throw new IllegalArgumentException("value is too big");
        }

        return new byte[] { (byte) ((v >>> 8) & 0xFF), (byte) ((v >>> 0) & 0xFF) };
    }

    /**
     * Used to express smaller sizes (2 bytes), since UDP Packets can be of up
     * to 65535 (including headers) Our chunk sizes should not be longer than
     * 65,400 bytes.
     * 
     * @param arr - array of at least 2 bytes
     * @param i - offset that leaves at least 2 bytes forward
     * @return
     */
    public static int byteArrayToSmallInt(byte[] arr, int offset) {
        if (arr == null || arr.length - offset < 2) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        return ((arr[offset] & 0xFF) << 8) + (arr[1 + offset] & 0xFF);
    }

    /**
     * Used to express smaller sizes (2 bytes), since UDP Packets can be of up
     * to 65535 (including headers) Our chunk sizes should not be longer than
     * 65,400 bytes.
     * @param arr - array of at least 2 bytes
     * @return
     */
    public static int byteArrayToSmallInt(byte[] arr) {
        return byteArrayToSmallInt(arr, 0);
    }

    // 3 bytes (16,777,215 - 16 MB)
    public static byte[] smallIntToTripleByteArray(int v) {
        if (v > 16777215) {
            throw new IllegalArgumentException("value is too big");
        }

        return new byte[] { (byte) ((v >>> 16) & 0xFF), (byte) ((v >>> 8) & 0xFF), (byte) ((v >>> 0) & 0xFF) };
    }

    /**
     * Used to express numbers up to 16,777,215
     * 
     * 
     * 
     * @param arr - array of at least 3 bytes
     * @param i - offset that leaves at least 3 bytes forward
     * @return
     */
    public static int tripleByteArrayToSmallInt(byte[] arr, int offset) {
        if (arr == null || arr.length - offset < 2) {
            return -1;
        }

        return (((arr[offset] & 0xFF) << 16) + ((arr[1 + offset] & 0xFF) << 8) + (arr[2 + offset] & 0xFF));
    }

    public static int tripleByteArrayToSmallInt(byte[] arr) {
        return tripleByteArrayToSmallInt(arr, 0);
    }

    /**
     * Returns a 3 byte array checksum number calculated on the given arr.
     * 
     * The checksum algorithm is very simple. We add up every N bytes
     * in the array, and return this number as a 3 byte array.
     * 
     * Currently we're jumping 16 bytes at the time. This takes up to 1 millisecond.
     * A 13mb made of 200 byte arrays of 65535 bytes parts can be checksumed
     * in 245ms on a Nexus One.
     * 
     * @param arr - An array of no more than 65535 bytes.
     * @return
     */
    public static byte[] getByteArrayChecksum(byte[] arr) {
        if (arr.length > 65535) {
            throw new IllegalArgumentException("Byte array is too long");
        }

        /** Max UDP packets are 65535 bytes long.
         *   Even if we used all of those bytes, the max number added would be */
        int result = 0;
        int step = (arr.length > 100) ? 16 : 1;

        for (int i = 0; i < arr.length; i += step) {
            result += (arr[i] & 0xFF);
        }

        return smallIntToTripleByteArray(result);
    }

    /**
     * Returns a new byte array. c = a + b. a will be towards the 0 index, b rigth after. c.length = a.length + b.length
     * @param a
     * @param b
     * @return
     */
    public static byte[] appendByteArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];

        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }

    public static int randomInt(int min, int max) {
        Random random = new Random(System.currentTimeMillis());
        return min + random.nextInt(max - min);
    }

    public static byte[] decodeHex(String str) {
        str = str.toLowerCase();
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    public static String encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }
        return new String(out);
    }
}
