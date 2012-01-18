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

import java.io.File;
import java.io.IOException;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * @param parentDir
     */
    public static File createFolder(File parentDir, String folderName) {
        File f = new File(parentDir, folderName);
        if (!f.exists() || !f.isDirectory()) {
            f.mkdir();
        }
        return f;
    }

    public static void deleteFolderRecursively(File folder) {
        if (folder != null && folder.isDirectory() && folder.canWrite()) {
            //delete your contents and recursively delete sub-folders
            File[] listFiles = folder.listFiles();
            for (File f : listFiles) {
                if (f.isFile()) {
                    f.delete();
                } else if (f.isDirectory()) {
                    deleteFolderRecursively(f);
                }
            }
            folder.delete();
        }
    }

    public static boolean deleteEmptyDirectoryRecursive(File directory) {
        // make sure we only delete canonical children of the parent file we
        // wish to delete. I have a hunch this might be an issue on OSX and
        // Linux under certain circumstances.
        // If anyone can test whether this really happens (possibly related to
        // symlinks), I would much appreciate it.
        String canonicalParent;
        try {
            canonicalParent = directory.getCanonicalPath();
        } catch (IOException ioe) {
            return false;
        }

        if (!directory.isDirectory()) {
            return false;
        }

        boolean canDelete = true;

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                if (!files[i].getCanonicalPath().startsWith(canonicalParent))
                    continue;
            } catch (IOException ioe) {
                canDelete = false;
            }

            if (!deleteEmptyDirectoryRecursive(files[i])) {
                canDelete = false;
            }
        }

        return canDelete ? directory.delete() : false;
    }
}
