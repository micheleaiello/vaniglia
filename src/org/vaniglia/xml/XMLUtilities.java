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
package org.vaniglia.xml;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class XMLUtilities {

    static private final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static private final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static private final String featureNameSchemaSource = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    static private Logger logger = Logger.getLogger(XMLUtilities.class);

    private static class LoggingErrorHandler implements ErrorHandler {

        static private Logger logger = Logger.getLogger(LoggingErrorHandler.class);

        public void error(SAXParseException e) throws SAXParseException {
            logger.error("Parsing error." +
                    "\n  Message        = " + e.getMessage() +
                    "\n  LineNumber     = " + e.getLineNumber() +
                    "\n  ColumnNumber   = " + e.getColumnNumber() +
                    "\n  PublicId       = " + e.getPublicId() +
                    "\n  SystemId       = " + e.getSystemId() +
                    "\n  Class          = " + e.getClass() +
                    "\n  Cause          = " + e.getCause() +
                    "\n  initCause(...) = " + e.initCause(e.getCause()) +
                    "\n  Exception      = " + e.getException()
            );
            throw e;
        }

        public void warning(SAXParseException e) {
            logger.warn("Parsing warning." +
                    "\n  Message        = " + e.getMessage() +
                    "\n  LineNumber     = " + e.getLineNumber() +
                    "\n  ColumnNumber   = " + e.getColumnNumber() +
                    "\n  PublicId       = " + e.getPublicId() +
                    "\n  SystemId       = " + e.getSystemId() +
                    "\n  Class          = " + e.getClass() +
                    "\n  Cause          = " + e.getCause() +
                    "\n  initCause(...) = " + e.initCause(e.getCause()) +
                    "\n  Exception      = " + e.getException()
            );
        }

        public void fatalError(SAXParseException e) throws SAXParseException {
            logger.fatal("Parsing fatalError." +
                    "\n  Message        = " + e.getMessage() +
                    "\n  LineNumber     = " + e.getLineNumber() +
                    "\n  ColumnNumber   = " + e.getColumnNumber() +
                    "\n  PublicId       = " + e.getPublicId() +
                    "\n  SystemId       = " + e.getSystemId() +
                    "\n  Class          = " + e.getClass() +
                    "\n  Cause          = " + e.getCause() +
                    "\n  initCause(...) = " + e.initCause(e.getCause()) +
                    "\n  Exception      = " + e.getException()
            );
            throw e;
        }
    }

    private final LoggingErrorHandler errorHandler = new LoggingErrorHandler();
    private DocumentBuilder db;

    public XMLUtilities() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        dbf.setIgnoringComments(true);
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.fatal("Unable to create a DocumentBuilder");
            System.exit(-1);
        }

        db.setErrorHandler(errorHandler);
    }

    public XMLUtilities(String schemaFileName) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setIgnoringComments(true);
        dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        dbf.setAttribute(featureNameSchemaSource, schemaFileName);
        try {
            db = dbf.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            logger.fatal("Unable to create a DocumentBuilder");
            System.exit(-1);
        }

        db.setErrorHandler(errorHandler);
    }

    public synchronized final Document getDocument(String fileName)
            throws XMLUtilitiesException
    {
        if ((fileName == null) || (fileName.equals(""))) {
            throw new XMLUtilitiesException();
        }

        Document document = null;

        try {
            FileInputStream in = new FileInputStream(fileName);

            document = db.parse(in);
            in.close();
        } catch (IOException e) {
            throw new XMLUtilitiesException();
        } catch (FactoryConfigurationError factoryConfigurationError) {
            factoryConfigurationError.printStackTrace();
            throw new XMLUtilitiesException();
        } catch (SAXException e) {
            e.printStackTrace();
            throw new XMLUtilitiesException();
        }

        return document;
    }

    public final void storeDocumentToFile(Document document, String fileName) {
        try {
            FileOutputStream outFile = new FileOutputStream(fileName);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(outFile);
            transformer.transform(source, result);
            outFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Java 1.5
//        OutputFormat format = new OutputFormat(document);
//        format.setLineWidth(65);
//        format.setIndenting(true);
//        format.setIndent(2);
//        try {
//            XMLSerializer serializer = new XMLSerializer(new FileOutputStream(outputFilename), format);
//            serializer.serialize(document);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public final void printDocumentToStream(Document inputDocument, OutputStream stream) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            DOMSource source = new DOMSource(inputDocument);
            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
            stream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Java 1.5
//        OutputFormat format = new OutputFormat(document);
//        format.setLineWidth(65);
//        format.setIndenting(true);
//        format.setIndent(2);
//        XMLSerializer serializer = new XMLSerializer(System.out, format);
//        try {
//            serializer.serialize(document);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public final Document getNewDocument() {
        Document document = db.newDocument();
        return document;
    }


}
