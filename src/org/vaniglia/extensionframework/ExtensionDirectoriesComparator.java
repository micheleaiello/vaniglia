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


package org.vaniglia.extensionframework;

import java.util.Comparator;
import java.io.File;

public class ExtensionDirectoriesComparator implements Comparator {

    private final char separator = '-';

    public int compare(Object o1, Object o2) {
        File f1 = (File) o1;
        File f2 = (File) o2;

        String dirname1 = f1.getName().trim();
        String dirname2 = f2.getName().trim();

        int sepIndex1 = dirname1.indexOf(separator);
        int sepIndex2 = dirname2.indexOf(separator);

        String order1Str = (sepIndex1 > 0)?dirname1.substring(0, sepIndex1).trim():null;
        String order2Str = (sepIndex2 > 0)?dirname2.substring(0, sepIndex2).trim():null;

        int order1 = (order1Str != null)?Integer.parseInt(order1Str):Integer.MAX_VALUE;
        int order2 = (order2Str != null)?Integer.parseInt(order2Str):Integer.MAX_VALUE;

        int retValue = order1 - order2;
        if (retValue == 0) {
            String name1 = (sepIndex1 > 0)?dirname1.substring(sepIndex1+1, dirname1.length()).trim():dirname1;
            String name2 = (sepIndex2 > 0)?dirname2.substring(sepIndex2+1, dirname2.length()).trim():dirname2;

            return name1.compareTo(name2);
        }
        else {
            return retValue;
        }
    }

}
