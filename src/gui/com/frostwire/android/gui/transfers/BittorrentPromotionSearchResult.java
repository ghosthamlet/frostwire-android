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

package com.frostwire.android.gui.transfers;

import com.frostwire.android.gui.PromotionsHandler.Slide;
import com.frostwire.android.gui.search.BittorrentSearchResult;
import com.frostwire.android.util.FilenameUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class BittorrentPromotionSearchResult implements BittorrentSearchResult {

    private final Slide slide;
    private final long creationTime;

    public BittorrentPromotionSearchResult(Slide slide) {
        this.slide = slide;
        this.creationTime = System.currentTimeMillis();
    }

    public String getTitle() {
        return slide.title;
    }

    @Override
    public long getSize() {
        return slide.size;
    }

    @Override
    public String getFileName() {
        return FilenameUtils.getName(slide.url);
    }

    @Override
    public int getSeeds() {
        return 0;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public int getSearchEngineId() {
        return -1;
    }

    @Override
    public String getTorrentDetailsURL() {
        return slide.url;
    }

    @Override
    public String getTorrentURI() {
        return slide.torrent;
    }

    @Override
    public String getVendor() {
        return "FrostClick";
    }
}
