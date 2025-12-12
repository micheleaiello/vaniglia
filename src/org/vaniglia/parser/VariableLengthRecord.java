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
package org.vaniglia.parser;

public class VariableLengthRecord extends Record {

    private static final int initialSize = 0;
    private Object[] fields;
    private int lastFieldIndex;

    public VariableLengthRecord(RecordType type) {
        super(type);
        fields = new Object[initialSize];
        lastFieldIndex = 0;
    }

    public void setFieldValue(int index, String value) throws InvalidFieldValueException {
        if (index >= fields.length) {
            int newDim = (fields.length==0)?(index+1):(Math.max(fields.length*2, index+1));
            Object[] newArray = new Object[newDim];
            System.arraycopy(fields, 0, newArray, 0, fields.length);
            fields = newArray;
        }
        fields[index] = type.fieldTypeAt(index).createField(value);
        lastFieldIndex = (index>lastFieldIndex)?index:lastFieldIndex;
    }

    public String getValue(int index){
        if ((index >= 0) && (index < fields.length)) {
            Object ret = fields[index];
            if(ret != null){
                return ret.toString();
            }
        }
        return "";
    }

    public Object getObject(int index){
        if((index >= 0) && (index < fields.length)) {
            Object ret = fields[index];
            if(ret != null){
                return ret;
            }
        }
        return null;
    }

    public int getNumberOfFields(){
        return lastFieldIndex+1;
    }

    protected void clear() {
        super.clear();
        fields = new Object[initialSize];
        lastFieldIndex = 0;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append('[');
        for (int i = 0; i < lastFieldIndex; i++) {
            if (fields[i] != null) {
                buff.append(fields[i].toString());
            }
            buff.append('|');
        }
        buff.append(fields[lastFieldIndex]);
        buff.append(']');
        return buff.toString();
    }

}
