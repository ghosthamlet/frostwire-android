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

import java.io.File;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;

import com.frostwire.android.util.FilenameUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
final class AzureusBittorrentDownloadItem implements BittorrentDownloadItem {

    private final DiskManagerFileInfo fileInfo;

    public AzureusBittorrentDownloadItem(DiskManagerFileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    @Override
    public String getDisplayName() {
        return FilenameUtils.getBaseName(fileInfo.getFile(false).getName());
    }

    public File getSavePath() {
        return fileInfo.getFile(false);
    }

    @Override
    public int getProgress() {
        return isComplete() ? 100 : (int) ((fileInfo.getDownloaded() * 100) / fileInfo.getLength());
    }

    @Override
    public long getSize() {
        return fileInfo.getLength();
    }

    @Override
    public boolean isComplete() {
        return fileInfo.getDownloaded() == fileInfo.getLength();
    }
}
