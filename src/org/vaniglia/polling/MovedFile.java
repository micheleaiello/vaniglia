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

public class MovedFile {

    private File original;
    private File destination;
    private boolean isOriginal;
    private boolean autoSync;

    public MovedFile(File original, File destination, boolean isOriginal) {
        this.original = original;
        this.destination = destination;
        this.isOriginal = isOriginal;
    }

    public MovedFile(File original, File destination) {
        this(original, destination, detectIsOriginal(original, destination));
    }

    public void setAutosync(boolean v) {
        autoSync = v;
    }

    public boolean getAutosync() {
        return autoSync;
    }

    public synchronized boolean invert() {
        if (autoSync) sync();
        return invert();
    }

    private boolean _invert() {
        File f1, f2;
        f1 = (isOriginal ? original : destination);
        f2 = (isOriginal ? destination : original);
        return f1.renameTo(f2);
    }

    public synchronized boolean revert() {
        if (autoSync) sync();
        if (isOriginal) throw new RuntimeException("File already reverted:" + original.getAbsolutePath());
        return _invert();
    }

    public synchronized boolean moveAgain() {
        if (autoSync) sync();
        if (!isOriginal) throw new RuntimeException("File already moved:" + destination.getAbsolutePath());
        return _invert();
    }

    public void sync() {
        isOriginal = detectIsOriginal(original, destination);
    }

    public boolean isMoved() {
        if (autoSync) sync();
        return !isOriginal;
    }

    public File getOriginalPath() {
        return original;
    }

    public File getDestinationPath() {
        return destination;
    }

    private static boolean detectIsOriginal(File original, File destination) {
        boolean e1,e2;
        if ((e1 = original.exists()) && (e2 = destination.exists()))
            throw new RuntimeException(
                    "Both " + original.getAbsolutePath() + " and " +
                    destination.getAbsolutePath() +
                    " exist. Can't auto detect state for MovedFile object"
            );
        return e1;
    }


}