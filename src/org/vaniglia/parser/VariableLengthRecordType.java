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

import java.util.HashMap;

public class VariableLengthRecordType extends AbstractRecordType {

    private FieldType[] fieldTypes;
    private String[] fieldNames;
    private FieldType extraFieldsType;
    private HashMap fieldsMap;

    public VariableLengthRecordType(FieldType[] fieldTypes, String[] fieldNames, FieldType extraFieldsType)  {
        this(fieldTypes, fieldNames, extraFieldsType, null, null);
    }

    public VariableLengthRecordType(FieldType[] fieldTypes, String[] fieldNames, FieldType extraFieldsType,
                                    boolean[] constantFields, RecordProcessor recordProcessor)
    {
        super(constantFields, recordProcessor);
        this.fieldTypes = fieldTypes;
        this.fieldNames = fieldNames;
        this.extraFieldsType = extraFieldsType;
        fieldsMap = new HashMap();
        for (int i = 0; i < fieldNames.length; i++) {
            if ((fieldNames[i] != null) && (!fieldNames[i].equals(""))) {
                fieldsMap.put(fieldNames[i], new Integer(i));
            }
        }
    }

    public FieldType fieldTypeAt(int index) {
        if (index < fieldTypes.length) {
            return fieldTypes[index];
        }
        else {
            return extraFieldsType;
        }
    }

    public FieldType[] getFieldTypes() {
        return fieldTypes;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public int getIndexOfField(String name) {
        Integer fieldIndex = (Integer)fieldsMap.get(name);
        if (fieldIndex != null) {
            return fieldIndex.intValue();
        } else {
            return -1;
        }
    }

    public void setFieldValue(Record record, int index, String value)
            throws InvalidFieldException, InvalidFieldValueException
    {
        ((VariableLengthRecord)record).setFieldValue(index, value);
    }

    protected Record _createRecord() {
        return new VariableLengthRecord(this);
    }

}
