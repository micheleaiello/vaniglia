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

import org.vaniglia.parser.*;
import org.vaniglia.parser.fieldtypes.StringType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;

public class MyCustomRecordType implements RecordType {

    private FieldType fieldType = new StringType(true);
    private int numberOfFields;
    private HashMap fieldsMap;
    private String[] fieldNames;

    public MyCustomRecordType(Element rootElement) {
        NodeList fieldElementList = rootElement.getElementsByTagName("Field");
        this.numberOfFields = fieldElementList.getLength();
        this.fieldsMap = new HashMap();
        this.fieldNames = new String[numberOfFields];
        for (int i = 0; i < this.numberOfFields; i++) {
            Element fieldElement = (Element) fieldElementList.item(i);
            String name = fieldElement.getAttribute("name");
            String positionStr = fieldElement.getAttribute("position");
            int position = 0;
            try {
                position = Integer.parseInt(positionStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                //LOG Invalid position.
                System.err.println("Invalid position '"+positionStr+"'");
                continue;
            }
            if (position > this.numberOfFields) {
                // LOG Invalid position.
                System.err.println("Invalid position '"+position+"' for field '"+name+"'");
                continue;
            }

            fieldsMap.put(name, new Integer(i));
            fieldNames[i] = name;
        }

    }

    public MyCustomRecordType(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public Record createRecord() {
        return new MyCustomRecord(this, numberOfFields);
    }

    public void releaseRecord(Record record) {
    }

    public FieldType fieldTypeAt(int index) {
        return fieldType;
    }

    public FieldType[] getFieldTypes() {
        FieldType[] types = new FieldType[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            types[i] = fieldType;
        }
        return types;
    }

    public String[] getFieldNames() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIndexOfField(String name) {
        return ((Integer)fieldsMap.get(name)).intValue();
    }

    public void setFieldValue(Record record, int index, String value)
            throws InvalidFieldException, InvalidFieldValueException
    {
        ((MyCustomRecord)record).setFieldValue(index, value);
    }


    public int getNextNonConstantFieldIndex(int index) {
        return (index>=0?(index+1):0);
    }


    public void initConstantFields(Record record) {
        return;
    }

    public void postProcessRecord(Record record) {
        // TODO Record post processing?
    }

}
