/**
 * Project Vaniglia
 * User: Michele Aiello
 *
 * Copyright (C) 2003/2007  Michele Aiello
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.vaniglia.messagequeue.storage.impl.fsimpl;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This FilenameFilter class is used to filter subdirectories based on the provided timestamp
 * and on the depth of the directory in the File System tree.
 */
public class SubdirectoriesFilter implements FilenameFilter {

    public static final int FIRST_LEVEL = 1;
    public static final int SECOND_LEVEL = 2;

    private long timestamp;
    private int level;

    public SubdirectoriesFilter() {
        this.timestamp = 0;
        this.level = 0;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDescription() {
        return null;
    }

    public boolean accept(File dir, String name) {
        if (name.equals(MessageStorageFileSystem.handlingSubdirName)) return false;

        long ts2 = timestamp/60000;
        long ts1 = timestamp/3600000;

        long nameTs = 0;
        try {
            nameTs = Long.parseLong(name);
        } catch (NumberFormatException e) {
            return false;
        }

        switch (level) {
            case FIRST_LEVEL: {
                return nameTs <= ts1;
            }
            case SECOND_LEVEL: {
                return nameTs <= ts2;
            }
        }

        return false;
    }

}
