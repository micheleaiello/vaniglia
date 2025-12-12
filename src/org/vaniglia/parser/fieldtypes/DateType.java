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

import java.text.ParseException;
import java.util.Date;

public class DateType extends FieldType {

    private SynchronizedSimpleDateFormat inputFormat;
    private SynchronizedSimpleDateFormat outputFormat;
    private boolean strongCheck = false;

    public DateType() {
    }

    public DateType(String inputFormat, String outputFormat) {
        this.inputFormat = SynchronizedSimpleDateFormat.getFormat(inputFormat);
        this.outputFormat = SynchronizedSimpleDateFormat.getFormat(outputFormat);
    }

    public boolean isStrongCheck() {
        return strongCheck;
    }

    public void setStrongCheck(boolean strongCheck) {
        this.strongCheck = strongCheck;
    }

    public void configure(Element rootElement) {
        String inputFormatStr = rootElement.getAttribute("inputFormat");
        String outputFormatStr = rootElement.getAttribute("outputFormat");

        this.inputFormat = SynchronizedSimpleDateFormat.getFormat(inputFormatStr);
        this.outputFormat = SynchronizedSimpleDateFormat.getFormat(outputFormatStr);

        String strongCheckStr = rootElement.getAttribute("strongCheck");
        if ((strongCheckStr != null) && (!strongCheckStr.equals(""))) {
            try {
                strongCheck = Boolean.valueOf(strongCheckStr).booleanValue();
            } catch (Exception e) {
                strongCheck = false;
            }
        }
    }

    protected Object _createField(String value) throws InvalidFieldValueException {
        Date date = null;
        value = value.trim();
        if (!value.equals("")) {
            try {
                date = this.inputFormat.parse(value);
            } catch (ParseException e) {
                throw new InvalidFieldValueException(value+" is not a valid date (format = "+inputFormat.toPattern()+")");
            }

            if (strongCheck) {
                String strongCheckValue = this.inputFormat.format(date);
                if (!strongCheckValue.equals(value)) {
                    throw new InvalidFieldValueException(value+" is not a valid date (format = "+inputFormat.toPattern()+") [Strong check = "+strongCheckValue+"]");
                }
            }
        }

        if (date != null) {
            return new DateField(date, outputFormat);
        }
        else {
            return null;
        }
    }

}
