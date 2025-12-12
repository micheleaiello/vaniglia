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
package org.vaniglia.time;

import java.io.PrintStream;
import java.text.NumberFormat;

public class TimeUtils {

    private static NumberFormat nf = NumberFormat.getInstance();

    public static void printMillis(long inputMillis, PrintStream out) {
        out.println(formatMillis(inputMillis));
    }

    public static String formatMillis(long inputMillis) {
//        long millis = inputMillis;
        long secs = inputMillis / 1000;
//        millis -= secs * 1000;
        long mins = inputMillis / 60000;
        secs -= mins * 60;
        long hours = inputMillis / 3600000;
        mins -= hours * 60;
        long days = inputMillis / 86400000;
        hours -= days * 24;

        StringBuffer buffer = new StringBuffer(40);
        buffer.append(days);
        buffer.append(" days ");
        buffer.append(hours);
        buffer.append(" hours ");
        buffer.append(mins);
        buffer.append(" minutes ");
        buffer.append(secs);
        buffer.append(" seconds (");
        buffer.append(nf.format(inputMillis));
        buffer.append(" ms)");

        return buffer.toString();
    }
}
