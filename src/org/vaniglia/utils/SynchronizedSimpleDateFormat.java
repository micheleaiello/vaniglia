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

package org.vaniglia.utils;

import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;
import java.text.ParseException;

public class SynchronizedSimpleDateFormat {

    private static final HashMap allFormats = new HashMap();

    private String format;
    private ThreadLocal threadLocal;

    public static final synchronized SynchronizedSimpleDateFormat getFormat(String format) {
        SynchronizedSimpleDateFormat retValue = (SynchronizedSimpleDateFormat) allFormats.get(format);
        if (retValue == null) {
            retValue = new SynchronizedSimpleDateFormat(format);
            allFormats.put(format, retValue);
        }

        return retValue;
    }

    private SynchronizedSimpleDateFormat(String format) {
        this.format = format;
        final String dateFormat = format;
        this.threadLocal = new ThreadLocal() {
            protected Object initialValue() {
                return new SimpleDateFormat(dateFormat);
            }
        };
    }

    public String format(Date date) {
        return ((SimpleDateFormat)threadLocal.get()).format(date);
    }

    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        return ((SimpleDateFormat)threadLocal.get()).format(date, toAppendTo, pos);
    }

    public Date parse(String source) throws ParseException {
        return ((SimpleDateFormat)threadLocal.get()).parse(source);
    }

    public String toPattern() {
        return ((SimpleDateFormat)threadLocal.get()).toPattern();
    }
}
