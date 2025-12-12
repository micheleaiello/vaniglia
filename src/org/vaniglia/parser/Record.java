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

public abstract class Record {

    protected RecordType type;
    protected String sourceRecord;

    protected Record(RecordType type) {
        this.type = type;
    }

    public RecordType getRecordType() {
        return type;
    }

    public String getSourceRecord() {
        return sourceRecord;
    }

    public void setSourceRecord(String sourceRecord) {
        this.sourceRecord = sourceRecord;
    }

    protected void clear() {
        this.sourceRecord = null;
    }

    public abstract String toString();

    public abstract void setFieldValue(int index, String value) throws InvalidFieldException, InvalidFieldValueException;

    public void setFieldValue(String name, String value) throws InvalidFieldException, InvalidFieldValueException {
        int index = type.getIndexOfField(name);
        if (index >= 0) {
            setFieldValue(index, value);
        }
        else {
            throw new InvalidFieldException("Unable to find field '"+name+"'.");
        }
    }

    public abstract String getValue(int index);

    public String getValue(String name) {
        int index = type.getIndexOfField(name);
        if (index >= 0) {
            return getValue(index);
        } else {
            return null;
        }
    }

    public abstract int getNumberOfFields();

    public abstract Object getObject(int index);

    public Object getObject(String name) {
        int index = type.getIndexOfField(name);
        if (index >= 0) {
            return getObject(index);
        }
        else {
            return null;
        }
    }

}
