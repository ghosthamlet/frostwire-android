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
package com.frostwire.android.gui.search;

import android.content.Intent;
import android.net.Uri;

/**
 * 
 * Represents a .torrent file that lives on a remote URL specified by the given Intent
 * a torrent download.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class BittorrentIntentHttpResult extends AbstractBittorrentIntentResult {

    private String uri;
    private Intent intent;
    
    public BittorrentIntentHttpResult(Intent intent) {
        this.intent = intent;
        uri = intent.getDataString();
    }

    @Override
    public String getTorrentURI() {
        return uri;
    }
}