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

package org.vaniglia.parser.fieldtypes;

import org.vaniglia.parser.FieldType;
import org.vaniglia.parser.InvalidFieldValueException;
import org.vaniglia.parser.fields.DateField;
import org.vaniglia.utils.SynchronizedSimpleDateFormat;
import org.w3c.dom.Element;

import java.util.Date;

public class TimestampType extends FieldType {

    private boolean isJavaTimestamp;
    private SynchronizedSimpleDateFormat outputFormat;

    public TimestampType() {
    }

    public TimestampType(boolean isJavaTimestamp, String outputFormat) {
        this.isJavaTimestamp = isJavaTimestamp;
        this.outputFormat = SynchronizedSimpleDateFormat.getFormat(outputFormat);
    }

    public boolean isJavaTimestamp() {
        return isJavaTimestamp;
    }

    public void setJavaTimestamp(boolean javaTimestamp) {
        isJavaTimestamp = javaTimestamp;
    }

    public void configure(Element rootElement) {
        String isJavaTimestampStr = rootElement.getAttribute("isJavaTimestamp");
        String outputFormatStr = rootElement.getAttribute("outputFormat");

        this.outputFormat = SynchronizedSimpleDateFormat.getFormat(outputFormatStr);

        if ((isJavaTimestampStr != null) && (!isJavaTimestampStr.equals(""))) {
            try {
                isJavaTimestamp = Boolean.valueOf(isJavaTimestampStr).booleanValue();
            } catch (Exception e) {
                isJavaTimestamp = false;
            }
        }
    }

    protected Object _createField(String value) throws InvalidFieldValueException {
        Date date = null;
        value = value.trim();
        if (!value.equals("")) {
            long millis = 0;
            try {
                millis = Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new InvalidFieldValueException(value+" is not a valid timestamp.");
            }
            if (!isJavaTimestamp) {
                millis = millis * 1000;
            }

            date = new Date(millis);
        }

        if (date != null) {
            return new DateField(date, outputFormat);
        }
        else {
            return null;
        }
    }

}
