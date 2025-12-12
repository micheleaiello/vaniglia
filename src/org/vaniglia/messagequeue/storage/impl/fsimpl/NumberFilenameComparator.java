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

import java.util.Comparator;
import java.io.File;

/**
 * Comparator that sorts directory for the MessageStoreFileSystem based on the
 * directory name.
 */
public class NumberFilenameComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        File f1 = (File) o1;
        File f2 = (File) o2;

        long ts1 = 0;
        try {
            ts1 = Long.parseLong(f1.getName());
        } catch (NumberFormatException e) {
        }

        long ts2 = 0;
        try {
            ts2 = Long.parseLong(f2.getName());
        } catch (NumberFormatException e) {
        }

        return ((ts1<ts2)?-1:((ts1==ts2)?0:1));
    }

}
