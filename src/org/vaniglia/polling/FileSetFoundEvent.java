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
package org.vaniglia.polling;

import java.io.File;

public class FileSetFoundEvent extends BasePollerEvent {

    private File[] files;
    private File directory;

    FileSetFoundEvent(DirectoryPoller poller, File dir, String[] paths) {
        super(poller);
        this.directory = dir;
        this.files = new File[paths.length];
        for (int i = 0; i < paths.length; i++)
            files[i] = new File(dir, paths[i]);
    }

    public File[] getFiles() {
        return files;
    }

    public File getDirectory() {
        return directory;
    }
}
