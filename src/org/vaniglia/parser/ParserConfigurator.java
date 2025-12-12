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

import org.vaniglia.xml.XMLUtilities;
import org.vaniglia.xml.XMLUtilitiesException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class ParserConfigurator {

    private static Class[] customTypeConstrArgs = {Element.class};

    public static Parser createParser(String xmlConfigFile) throws ParserConfigurationException {
        //TODO Schema checking
        XMLUtilities xmlUtils = new XMLUtilities();

        Document document = null;
        try {
            document = xmlUtils.getDocument(xmlConfigFile);
        } catch (XMLUtilitiesException e) {
            throw new ParserConfigurationException("Unable to read configuration file '"+xmlConfigFile+"'", e);
        }

        Element rootElement = document.getDocumentElement();

        HashMap typeDefMap = new HashMap();
        try {
            initializeDefaultTypes(typeDefMap);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HashMap preProcessorsDefMap = new HashMap();
        try {
            initializeDefaultPreProcessors(preProcessorsDefMap);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HashMap validatorDefMap = new HashMap();
        try {
            initializeDefaultValidators(validatorDefMap);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        {
            NodeList fieldTypeDefList = rootElement.getElementsByTagName("FieldTypeDef");
            int numOfDefs = fieldTypeDefList.getLength();
            for (int i = 0; i < numOfDefs; i++) {
                String typeClassStr = null;
                try {
                    Element fieldTypeDefElement = (Element) fieldTypeDefList.item(i);
                    String name = fieldTypeDefElement.getAttribute("name");
                    typeClassStr = fieldTypeDefElement.getAttribute("class");
                    Class typeClass = Class.forName(typeClassStr);

                    Class[] interfaces = typeClass.getInterfaces();
                    boolean hasRecordTypeInterface = false;
                    for (int k = 0; k < interfaces.length; k++) {
                        if (interfaces.equals(RecordType.class)) {
                            hasRecordTypeInterface = true;
                            break;
                        }
                    }

                    if (!hasRecordTypeInterface) {
                        throw new ParserConfigurationException("Class "+typeClassStr+" must implement "+RecordType.class.getName());
                    }

                    typeDefMap.put(name, typeClass);
                } catch (ClassNotFoundException e) {
                    throw new ParserConfigurationException("Unable to find class '"+typeClassStr+"'", e);
                }
            }
        }

        {
            NodeList preProcessorsDefList = rootElement.getElementsByTagName("PreProcessorDef");
            int numOfPreProcessorsDefs = preProcessorsDefList.getLength();
            for (int i = 0; i < numOfPreProcessorsDefs; i++) {
                String preProcessorClassStr = null;
                try {
                    Element preProcessorDefElement = (Element) preProcessorsDefList.item(i);
                    String name = preProcessorDefElement.getAttribute("name");
                    preProcessorClassStr = preProcessorDefElement.getAttribute("class");
                    Class preProcessorClass = Class.forName(preProcessorClassStr);
                    Class[] interfaces = preProcessorClass.getInterfaces();
                    boolean hasPreProcessorInterface = false;
                    for (int k = 0; k < interfaces.length; k++) {
                        if (interfaces[k].equals(FieldPreProcessor.class)) {
                            hasPreProcessorInterface = true;
                            break;
                        }
                    }

                    if (!hasPreProcessorInterface) {
                        throw new ParserConfigurationException("Class "+preProcessorClassStr+" must implement "+FieldPreProcessor.class.getName());
                    }

                    preProcessorsDefMap.put(name, preProcessorClass);
                } catch (ClassNotFoundException e) {
                    throw new ParserConfigurationException("Unable to find class '"+preProcessorClassStr+"'", e);
                }
            }
        }

        {
            NodeList validatorDefList = rootElement.getElementsByTagName("ValidatorDef");
            int numOfValidatorDefs = validatorDefList.getLength();
            for (int i = 0; i < numOfValidatorDefs; i++) {
                String validatorClassStr = null;
                try {
                    Element validatorDefElement = (Element) validatorDefList.item(i);
                    String name = validatorDefElement.getAttribute("name");
                    validatorClassStr = validatorDefElement.getAttribute("class");
                    Class validatorClass = Class.forName(validatorClassStr);
                    Class[] interfaces = validatorClass.getInterfaces();
                    boolean hasValidatorInterface = false;
                    for (int k = 0; k < interfaces.length; k++) {
                        if (interfaces[k].equals(Validator.class)) {
                            hasValidatorInterface = true;
                            break;
                        }
                    }

                    if (!hasValidatorInterface) {
                        throw new ParserConfigurationException("Class "+validatorClassStr+" must implement "+Validator.class.getName());
                    }

                    validatorDefMap.put(name, validatorClass);
                } catch (ClassNotFoundException e) {
                    throw new ParserConfigurationException("Unable to find class '"+validatorClassStr+"'", e);
                }
            }
        }

        Element parserElement = (Element) rootElement.getElementsByTagName("Parser").item(0);
        char token = Parser.DEFAULT_TOKEN;
        String tokenStr = parserElement.getAttribute("token");
        if (tokenStr.length() > 0) {
            token = tokenStr.charAt(0);
        }

        char comment = Parser.DEFAULT_COMMENT;
        String commentStr = parserElement.getAttribute("comment");
        if (commentStr.length() > 0) {
            comment = commentStr.charAt(0);
        }

        int recovery = 0;
        String recoveryStr = parserElement.getAttribute("recovery");
        try {
            recovery = Integer.parseInt(recoveryStr);
        } catch (NumberFormatException e) {
            recovery = 0;
        }

        boolean flush = false;
        String flushStr = parserElement.getAttribute("flush");
        try {
            flush = Boolean.valueOf(flushStr).booleanValue();
        } catch (Exception e) {
            flush = false;
        }

        RecordType recordType = null;

        NodeList fixedLenghtRecordTypeList = parserElement.getElementsByTagName("FixedLenghtRecordType");

        if (fixedLenghtRecordTypeList.getLength() > 0) {
            Element fixedLenghtRecordTypeElement = (Element) fixedLenghtRecordTypeList.item(0);
            NodeList fieldTypeList = fixedLenghtRecordTypeElement.getChildNodes();

            int numOfFields = fieldTypeList.getLength();
            ArrayList fieldList = new ArrayList();
            ArrayList fieldNameList = new ArrayList();
            ArrayList fieldIsConstant = new ArrayList();
            RecordProcessor recordProcessor = null;

            for (int i = 0; i < numOfFields; i++) {
                if (fieldTypeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element fieldTypeElement = (Element) fieldTypeList.item(i);
                if (fieldTypeElement.getTagName().equals("RecordProcessor")) {
                    try {
                        recordProcessor = new RecordProcessor(fieldTypeElement);
                    } catch (RecordProcessorException e) {
                        e.printStackTrace();
                        recordProcessor = null;
                    }
                }
                else {
                    FieldType fieldType = getFieldType(fieldTypeElement, typeDefMap, preProcessorsDefMap, validatorDefMap);
                    fieldList.add(fieldType);

                    String fieldName = fieldTypeElement.getAttribute("name");
                    String defaultValue = fieldTypeElement.getAttribute("default");
                    String isConstantStr = fieldTypeElement.getAttribute("isConstant");

                    fieldNameList.add(fieldName);
                    if ((fieldName != null) && (!fieldName.equals(""))) {
                        fieldType.setName(fieldName);
                    }
                    if ((defaultValue != null) && (!defaultValue.equals(""))) {
                        fieldType.setDefaultValue(defaultValue);
                    }
                    fieldIsConstant.add(Boolean.valueOf(isConstantStr));
                }
            }

            FieldType[] fieldTypes = new FieldType[0];
            fieldTypes = (FieldType[]) fieldList.toArray(fieldTypes);

            String[] fieldNames = new String[0];
            fieldNames = (String[]) fieldNameList.toArray(fieldNames);

            boolean[] fieldConstants = new boolean[fieldIsConstant.size()];
            for (int i = 0; i < fieldConstants.length; i++) {
                fieldConstants[i] = ((Boolean)fieldIsConstant.get(i)).booleanValue();
            }

            recordType = new FixedLengthRecordType(fieldTypes, fieldNames, fieldConstants, recordProcessor);
        }
        else {
            NodeList variableLenghtRecordTypeList = parserElement.getElementsByTagName("VariableLenghtRecordType");
            if (variableLenghtRecordTypeList.getLength() > 0) {
                Element variableLenghtRecordTypeElement = (Element) variableLenghtRecordTypeList.item(0);
                NodeList fieldTypeList = variableLenghtRecordTypeElement.getChildNodes();
                int numOfFields = fieldTypeList.getLength();
                ArrayList fieldList = new ArrayList();
                ArrayList fieldNameList = new ArrayList();
                ArrayList fieldIsConstant = new ArrayList();
                RecordProcessor recordProcessor = null;
                FieldType extraFieldsType = null;

                for (int i = 0; i < numOfFields; i++) {
                    if (fieldTypeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element fieldTypeElement = (Element) fieldTypeList.item(i);
                    if (fieldTypeElement.getTagName().equals("RecordProcessor")) {
                        try {
                            recordProcessor = new RecordProcessor(fieldTypeElement);
                        } catch (RecordProcessorException e) {
                            e.printStackTrace();
                            recordProcessor = null;
                        }
                    }
                    else if (fieldTypeElement.getTagName().equals("ExtraFieldsType")) {
                        NodeList extraElementList = fieldTypeElement.getChildNodes();
                        int numberOfChilds = extraElementList.getLength();
                        int j = 0;
                        while ((j < numberOfChilds) && (extraFieldsType == null)) {
                            if (extraElementList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element extraFieldTypeElement = (Element) extraElementList.item(j);
                                extraFieldsType = getFieldType(extraFieldTypeElement, typeDefMap, preProcessorsDefMap, validatorDefMap);

                                String defaultValue = extraFieldTypeElement.getAttribute("default");
                                if ((defaultValue != null) && (!defaultValue.equals(""))) {
                                    extraFieldsType.setDefaultValue(defaultValue);
                                }
                            }
                            j++;
                        }
                    }
                    else {
                        FieldType fieldType = getFieldType(fieldTypeElement, typeDefMap, preProcessorsDefMap, validatorDefMap);
                        fieldList.add(fieldType);

                        String fieldName = fieldTypeElement.getAttribute("name");
                        String defaultValue = fieldTypeElement.getAttribute("default");
                        String isConstantStr = fieldTypeElement.getAttribute("isConstant");

                        fieldNameList.add(fieldName);
                        if ((fieldName != null) && (!fieldName.equals(""))) {
                            fieldType.setName(fieldName);
                        }
                        if ((defaultValue != null) && (!defaultValue.equals(""))) {
                            fieldType.setDefaultValue(defaultValue);
                        }
                        fieldIsConstant.add(Boolean.valueOf(isConstantStr));
                    }
                }

                FieldType[] fieldTypes = new FieldType[0];
                fieldTypes = (FieldType[]) fieldList.toArray(fieldTypes);

                String[] fieldNames = new String[0];
                fieldNames = (String[]) fieldNameList.toArray(fieldNames);

                boolean[] fieldConstants = new boolean[fieldIsConstant.size()];
                for (int i = 0; i < fieldConstants.length; i++) {
                    fieldConstants[i] = ((Boolean)fieldIsConstant.get(i)).booleanValue();
                }

                recordType = new VariableLengthRecordType(fieldTypes, fieldNames, extraFieldsType, fieldConstants, recordProcessor);
            }
            else {
                NodeList customRecordTypeList = parserElement.getElementsByTagName("CustomRecordType");
                Element customRecordTypeElement = (Element) customRecordTypeList.item(0);
                String customClassName = customRecordTypeElement.getAttribute("class");
                try {
                    Class customClass = Class.forName(customClassName);
                    Constructor constructor = customClass.getConstructor(customTypeConstrArgs);
                    Object[] params = {customRecordTypeElement};
                    recordType = (RecordType) constructor.newInstance(params);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new ParserConfigurationException(e);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    throw new ParserConfigurationException(e);
                } catch (SecurityException e) {
                    e.printStackTrace();
                    throw new ParserConfigurationException(e);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new ParserConfigurationException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new ParserConfigurationException(e);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    throw new ParserConfigurationException(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new ParserConfigurationException(e);
                }
            }

        }

        Parser parser = new Parser(recordType, token, comment, recovery);
        parser.setFlush(flush);
        return parser;
    }

    private static FieldType getFieldType(Element fieldTypeElement, HashMap typeDefMap,
                                          HashMap preProcessorsDefMap, HashMap validatorDefMap)
            throws ParserConfigurationException
    {
        String fieldTypeName = fieldTypeElement.getTagName();
        Class typeClass = (Class) typeDefMap.get(fieldTypeName);
        if (typeClass == null) {
            throw new ParserConfigurationException("Undefined type '"+fieldTypeName+"'");
        }

        FieldType fieldType = null;

        try {
            fieldType = (FieldType) typeClass.newInstance();
            fieldType.configure(fieldTypeElement);
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        }

        NodeList preProcessorsRootList = fieldTypeElement.getElementsByTagName("PreProcessor");
        if (preProcessorsRootList.getLength() > 0) {
            Element preProcessorsRootElement = (Element) preProcessorsRootList.item(0);
            NodeList preProcessorsList = preProcessorsRootElement.getChildNodes();
            int numOfPreProcessors = preProcessorsList.getLength();
            for (int i = 0; i < numOfPreProcessors; i++) {
                if (preProcessorsList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element preProcessorElement = (Element) preProcessorsList.item(i);
                FieldPreProcessor preProcessor = getPreProcessor(preProcessorElement, preProcessorsDefMap);
                fieldType.addPreProcessor(preProcessor);
            }
        }

        NodeList validatorsRootList = fieldTypeElement.getElementsByTagName("Validator");
        if (validatorsRootList.getLength() > 0) {
            Element validatorsRootElement = (Element) validatorsRootList.item(0);
            NodeList validatorsList = validatorsRootElement.getChildNodes();
            int numOfValidators = validatorsList.getLength();
            for (int i = 0; i < numOfValidators; i++) {
                if (validatorsList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element validatorElement = (Element) validatorsList.item(i);
                Validator validator = getValidator(validatorElement, validatorDefMap);
                fieldType.addValidator(validator);
            }
        }

        return fieldType;
    }

    private static FieldPreProcessor getPreProcessor(Element preProcessorElement, HashMap preProcessorsDefMap) throws ParserConfigurationException {
        String preProcessorClassName = preProcessorElement.getTagName();
        Class preProcessorClass = (Class) preProcessorsDefMap.get(preProcessorClassName);
        if (preProcessorClass == null) {
            throw new ParserConfigurationException("Undefined PreProcessor '"+preProcessorClassName+"'");
        }

        FieldPreProcessor preProcessor = null;

        try {
            preProcessor = (FieldPreProcessor) preProcessorClass.newInstance();
            preProcessor.configure(preProcessorElement);
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        }

        return preProcessor;
    }

    private static Validator getValidator(Element validatorElement, HashMap validatorDefMap) throws ParserConfigurationException {
        String validatorClassName = validatorElement.getTagName();
        Class validatorClass = (Class) validatorDefMap.get(validatorClassName);
        if (validatorClass == null) {
            throw new ParserConfigurationException("Undefined validator '"+validatorClassName+"'");
        }

        Validator validator = null;

        try {
            validator = (Validator) validatorClass.newInstance();
            validator.configure(validatorElement);
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new ParserConfigurationException(e);
        }

        return validator;
    }

    private static void initializeDefaultTypes(HashMap typeDefMap) throws ClassNotFoundException {
        typeDefMap.put("StringFieldType", Class.forName("org.vaniglia.parser.fieldtypes.StringType"));
        typeDefMap.put("BooleanFieldType", Class.forName("org.vaniglia.parser.fieldtypes.BooleanType"));
        typeDefMap.put("DateFieldType", Class.forName("org.vaniglia.parser.fieldtypes.DateType"));
        typeDefMap.put("TimestampFieldType", Class.forName("org.vaniglia.parser.fieldtypes.TimestampType"));
        typeDefMap.put("CharFieldType", Class.forName("org.vaniglia.parser.fieldtypes.CharType"));
    }

    private static void initializeDefaultPreProcessors(HashMap preProcessorsDefMap) throws ClassNotFoundException {
        preProcessorsDefMap.put("SubstringPreProcessor", Class.forName("org.vaniglia.parser.fieldpreprocessors.SubstringPreProcessor"));
        preProcessorsDefMap.put("TrimPreProcessor", Class.forName("org.vaniglia.parser.fieldpreprocessors.TrimPreProcessor"));
        preProcessorsDefMap.put("StringAppenderPreProcessor", Class.forName("org.vaniglia.parser.fieldpreprocessors.StringAppenderPreProcessor"));
    }

    private static void initializeDefaultValidators(HashMap validatorDefMap) throws ClassNotFoundException {
        validatorDefMap.put("NotNullValidator", Class.forName("org.vaniglia.parser.validators.NotNullValidator"));
        validatorDefMap.put("CharsetValidator", Class.forName("org.vaniglia.parser.validators.CharsetValidator"));
        validatorDefMap.put("LengthValidator", Class.forName("org.vaniglia.parser.validators.LengthValidator"));
    }

}
