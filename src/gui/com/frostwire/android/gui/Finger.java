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

package com.frostwire.android.gui;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class Finger {

    // general data

    public String uuid;

    public String nickname;

    public String frostwireVersion;

    public int totalShared;

    // device data

    /**
     * The user-visible version string.
     */
    public String deviceVersion;

    /**
     * The end-user-visible name for the end product.
     */
    public String deviceModel;

    /**
     * The name of the overall product.
     */
    public String deviceProduct;

    /**
     * The name of the industrial design.
     */
    public String deviceName;

    /**
     * The manufacturer of the product/hardware.
     */
    public String deviceManufacturer;

    /**
     * The brand (e.g., carrier) the software is customized for.
     */
    public String deviceBrand;

    /**
     * Screen metrics that describe the size and density of this screen.
     */
    public ScreenMetrics deviceScreen;

    // shared data

    public int numSharedAudioFiles;

    public int numSharedVideoFiles;

    public int numSharedPictureFiles;

    public int numSharedDocumentFiles;

    public int numSharedApplicationFiles;

    public int numSharedRingtoneFiles;

    // total data

    public int numTotalAudioFiles;

    public int numTotalVideoFiles;

    public int numTotalPictureFiles;

    public int numTotalDocumentFiles;

    public int numTotalApplicationFiles;

    public int numTotalRingtoneFiles;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        sb.append(nickname + ", " + totalShared + ", " + " sc:" + deviceScreen.widthPixels + "x" + deviceScreen.heightPixels);
        sb.append("[");
        sb.append("aud:" + numSharedAudioFiles + "/" + numTotalAudioFiles + ", ");
        sb.append("vid:" + numSharedVideoFiles + "/" + numTotalVideoFiles + ", ");
        sb.append("pic:" + numSharedPictureFiles + "/" + numTotalPictureFiles + ", ");
        sb.append("doc:" + numSharedDocumentFiles + "/" + numTotalDocumentFiles + ", ");
        sb.append("app:" + numSharedApplicationFiles + "/" + numTotalApplicationFiles + ", ");
        sb.append("rng:" + numSharedRingtoneFiles + "/" + numTotalRingtoneFiles);
        sb.append("]");
        sb.append(")");

        return sb.toString();
    }
}
