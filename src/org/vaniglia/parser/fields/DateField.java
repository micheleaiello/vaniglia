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
package org.vaniglia.parser.fields;

import org.vaniglia.utils.SynchronizedSimpleDateFormat;

import java.util.Date;

public class DateField {

    private Date date;
    private SynchronizedSimpleDateFormat outputFormat;

    public DateField(Date date, SynchronizedSimpleDateFormat outputFormat) {
        this.outputFormat = outputFormat;
        this.date = date;
    }

    public Date getDate(){
        return date;
    }

    public String toString() {
        if (date != null) {
            return outputFormat.format(date);
        }
        else {
            return "";
        }
    }

}
