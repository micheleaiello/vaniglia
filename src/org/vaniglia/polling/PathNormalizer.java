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
import java.util.*;


/**
 * An utility class to compute normalized paths (i.e. paths that do not contain
 * neither "." nor "..")
 */
public class PathNormalizer {

    private static HashMap normalizedPaths = new HashMap();

    public static File normalize(File path) {
        File normalized = (File) normalizedPaths.get(path);
        if (normalized == null) {
            normalized = new File(normalizePath(path.getAbsolutePath()));
            normalizedPaths.put(path, normalized);
        }
        return normalized;
    }

    public static String normalizePath(String path) {
        File f = new File(path);
        boolean trailingFSep = path.endsWith(File.separator);
        boolean leadingFSep = path.startsWith(File.separator);

        path = f.getAbsolutePath();
        StringTokenizer st = new StringTokenizer(path, File.separator);
        List names = new ArrayList();
        while (st.hasMoreTokens()) {
            String step = st.nextToken();
            if (".".equals(step))
                continue;
            else if ("..".equals(step))
                names.remove(names.size() - 1);
            else
                names.add(step);
        }

        StringBuffer sb = new StringBuffer();
        if (leadingFSep) {
            sb.append(File.separator);
        }
        for (Iterator i = names.iterator(); i.hasNext();) {
            sb.append(i.next());
            if (i.hasNext()) sb.append(File.separator);
        }
        if (trailingFSep) {
            sb.append(File.separator);
        }
        return sb.toString();
    }

}