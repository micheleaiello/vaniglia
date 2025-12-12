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
package org.vaniglia.examples.parser;

import org.vaniglia.parser.InvalidFieldException;
import org.vaniglia.parser.Record;
import org.vaniglia.parser.RecordType;

public class MyCustomRecord extends Record {

    private String[] values;
    private int numberOfFields;

    public MyCustomRecord(RecordType type, int numberOfFields) {
        super(type);
        this.numberOfFields = numberOfFields;
        this.values = new String[this.numberOfFields];
    }

    public void setFieldValue(int index, String value) throws InvalidFieldException {
        values[index] = value.trim();
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public String getValue(int index){
        if ((index >= 0) && (index < values.length)) {
            Object ret = values[index];
            if(ret != null){
                return ret.toString();
            }
        }
        return "";
    }

    public Object getObject(int index){
        if((index >= 0) && (index < values.length)) {
            Object ret = values[index];
            if(ret != null){
                return ret;
            }
        }
        return null;
    }

    protected void clear() {
        super.clear();
        this.values = new String[numberOfFields];
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append('[');
        for (int i = 0; i < values.length-1; i++) {
            if (values[i] != null) {
                buff.append(values[i]);
            }
            buff.append('|');
        }
        buff.append(values[values.length-1]);
        buff.append(']');
        buff.append(" - MyCustomRecord");
        return buff.toString();
    }

}
