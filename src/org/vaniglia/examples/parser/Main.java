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
import org.vaniglia.parser.fieldtypes.DateType;
import org.vaniglia.parser.fieldtypes.StringType;

public class Main {

    public static void main(String[] args) throws ParserException, ParserConfigurationException, InvalidFieldValueException {
        // Parser Created Programmatically
        {
            System.out.println("Parser Created Programmatically");

            StringType fieldType1 = new StringType(true);
            DateType fieldType2 = new DateType("yyyyMMdd", "dd/MM/yyyy");
            StringType fieldType3 = new StringType(true);

            FieldType[] types = {fieldType1, fieldType2, fieldType3};
            String[] names = {"F1", "F2", "F3"};

            Parser parser = new Parser(new FixedLengthRecordType(types, names));

            Record[] records = parser.parse("tsv/Test.tsv");
            for (int i = 0; i < records.length; i++) {
                System.out.println(records[i]);
            }
            parser.releaseRecords(records);
            System.out.println();
        }

        // Fixed Fields Parser from XML configuration file
        {
            System.out.println("Fixed Fields Parser from XML configuration file");
            Parser parser = ParserConfigurator.createParser("xml/parser/Parser.xml");
            Record[] records = parser.parse("tsv/Test.tsv");
            for (int i = 0; i < records.length; i++) {
                System.out.println(records[i]);
            }
            parser.releaseRecords(records);
            System.out.println();
        }

        // Fixed Fields Parser from XML configuration file
        {
            System.out.println("Fixed Fields Parser from XML configuration file. Line by line parsing");
            Parser parser = ParserConfigurator.createParser("xml/parser/Parser.xml");
            parser.open("tsv/Test.tsv");
            while (parser.hasMoreRecords()) {
                Record record = parser.getNextRecord();
                System.out.println(record);
                parser.releaseRecord(record);
            }
            parser.close();
            System.out.println();
        }

        // Variable Fields Parser from XML configuration file
        {
            System.out.println("Variable Fields Parser from XML configuration file");
            Parser parser = ParserConfigurator.createParser("xml/parser/VariableFieldsParser.xml");
            Record[] records = parser.parse("tsv/Test.tsv");
            for (int i = 0; i < records.length; i++) {
                System.out.println(records[i]);
            }
            parser.releaseRecords(records);
            System.out.println();
        }

        // Custom Type Parser from XML configuration file
        {
            System.out.println("Custom Type Parser from XML configuration file");
            Parser parser = ParserConfigurator.createParser("xml/parser/CustomTypeParser.xml");
            Record[] records = parser.parse("tsv/Custom.tsv");
            for (int i = 0; i < records.length; i++) {
                System.out.println(records[i]);
            }
            parser.releaseRecords(records);
            System.out.println();
        }

        // Parser with Validation
        {
            System.out.println("Parser with Validation");
            Parser parser = ParserConfigurator.createParser("xml/parser/ValidatorParser.xml");
            Record[] records = parser.parse("tsv/ValidatorTest.tsv");
            for (int i = 0; i < records.length; i++) {
                System.out.println(records[i]);
            }
            parser.releaseRecords(records);
            System.out.println();
        }

    }

}
