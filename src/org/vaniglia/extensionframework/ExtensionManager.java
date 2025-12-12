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

package org.vaniglia.extensionframework;

import org.apache.log4j.Logger;
import org.vaniglia.xml.XMLUtilities;
import org.vaniglia.xml.XMLUtilitiesException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

public class ExtensionManager {

    private final XMLFileFilter xmlFileFilter = new XMLFileFilter();
    private final ExtensionDirectoriesComparator extDirComparator = new ExtensionDirectoriesComparator();

    private class DirectoryFilter implements FileFilter {
        public boolean accept(File pathname) {
            return (pathname.isDirectory() && (!pathname.getName().startsWith("X")));
        }
    }

    private class JarFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jar");
        }
    }

    private static Logger logger = Logger.getLogger(ExtensionManager.class);

    private XMLUtilities xmlUtilities = new XMLUtilities();
    private final DirectoryFilter directoryFilter = new DirectoryFilter();
    private final JarFilter jarFilter = new JarFilter();

    private static ExtensionManager _instance;

    private static HashMap extensionsTable;
    private static HashMap extensionPointsTable;
    private static HashMap extensionPointsClassTable;
    private static HashMap extensionPointsIdentiferTable;

    private static HashMap extensionsClassloadersTable;

    private static boolean extensionsParametersWatchDogEnabled = true;
    private static boolean extensionsGlobalParametersWatchDogEnabled = true;
    private static boolean extensionsWatchDogEnabled = true;

    private static int watchDogReloadPeriod = 10000;

    private ExtensionManager() {
        extensionsTable = new HashMap();
        extensionPointsTable = new HashMap();
        extensionPointsClassTable = new HashMap();
        extensionPointsIdentiferTable = new HashMap();

        extensionsClassloadersTable = new HashMap();
    }

    public static synchronized final ExtensionManager getInstance() {
        if (_instance == null) {
            _instance = new ExtensionManager();
        }
        return _instance;
    }

    public static boolean isExtensionsParametersWatchDogEnabled() {
        return extensionsParametersWatchDogEnabled;
    }

    public static void setExtensionsParametersWatchDogEnabled(boolean extensionsParametersWatchDogEnabled) {
        ExtensionManager.extensionsParametersWatchDogEnabled = extensionsParametersWatchDogEnabled;
    }

    public static boolean isExtensionsWatchDogEnabled() {
        return extensionsWatchDogEnabled;
    }

    public static void setExtensionsWatchDogEnabled(boolean extensionsWatchDogEnabled) {
        ExtensionManager.extensionsWatchDogEnabled = extensionsWatchDogEnabled;
    }

    public static boolean isExtensionsGlobalParametersWatchDogEnabled() {
        return extensionsGlobalParametersWatchDogEnabled;
    }

    public static void setExtensionsGlobalParametersWatchDogEnabled(boolean extensionsGlobalParametersWatchDogEnabled) {
        ExtensionManager.extensionsGlobalParametersWatchDogEnabled = extensionsGlobalParametersWatchDogEnabled;
    }

    public static int getWatchDogReloadPeriod() {
        return watchDogReloadPeriod;
    }

    public static void setWatchDogReloadPeriod(int watchDogReloadPeriod) {
        ExtensionManager.watchDogReloadPeriod = watchDogReloadPeriod;
    }

    public void loadExtensionPoints(String configFile) throws ExtensionManagerException {
        logger.info("Loading Extension Points from file '"+configFile+"'...");
        File file = new File(configFile);
        if (!file.exists()) {
            logger.error("Unable to load Extension Points because the configuration file '"+configFile+"' doesn't exists");
            throw new ExtensionManagerException("Unable to load Extension Points because the configuration file '"+configFile+"' doesn't exists");
        }

        Document document = null;
        try {
            document = xmlUtilities.getDocument(configFile);
        } catch (XMLUtilitiesException e) {
            logger.error("Unable to load configuration file '"+configFile+"'.", e);
            throw new ExtensionManagerException("Unable to load configuration file '"+configFile+"'.");
        }

        Element rootElement = document.getDocumentElement();

        logger.info("Loading Global Parameters...");
        NodeList globalParamNodeList = rootElement.getElementsByTagName("GlobalParameters");
        if (globalParamNodeList.getLength() > 0) {
            Element globalParametersRoot = (Element) globalParamNodeList.item(0);
            GlobalParameters.getInstance().loadGlobalParameters(globalParametersRoot);
            if (ExtensionManager.isExtensionsGlobalParametersWatchDogEnabled()) {
                logger.info("Starting Global Parameters Watch Dog...");
                GlobalParametersWatchDog watchDog = new GlobalParametersWatchDog(configFile);
                watchDog.setReloadPeriod(watchDogReloadPeriod);
                watchDog.start();
                logger.info("done!");

            }
        }

        NodeList pointsList = rootElement.getElementsByTagName("ExtensionPoint");
        int numOfPoints = pointsList.getLength();
        for (int i = 0; i < numOfPoints; i++) {
            Element extPointElement = (Element) pointsList.item(i);
            try {
                loadExtensionPoint(extPointElement, null);
            } catch (ExtensionManagerException e) {
                continue;
            } catch (InitializationException e) {
                continue;
            } catch (Throwable e) {
                String name = extPointElement.getAttribute("name");
                logger.error("Exception loading Extension Point '"+name+"'.", e);
                continue;
            }
        }
    }

    private void loadExtensionPoint(Element extPointElement, ClassLoader classLoader)
            throws ExtensionManagerException, InitializationException
    {
        String extPointName = extPointElement.getAttribute("name");
        String className = extPointElement.getAttribute("className");
        logger.info("Loading Extension Point' "+extPointName+"'...");

        NodeList parametersList = extPointElement.getElementsByTagName("Parameter");
        int numOfParams = parametersList.getLength();
        ExtensionPointParameters parameters = new ExtensionPointParameters();
        for (int p = 0; p < numOfParams; p++) {
            Element paramElement = (Element) parametersList.item(p);
            String paramname = paramElement.getAttribute("name");
            String paramvalue = paramElement.getAttribute("value");
            parameters.addParameter(paramname, paramvalue);
        }

        Class extensionPointClass = null;
        try {
            logger.info("Loading Class for Extension Point '"+extPointName+"'...");
            if (classLoader != null) {
                try {
                    extensionPointClass = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    logger.error("Unable to find Class for Extension Point '"+extPointName+"'.", e);
                    throw new ExtensionManagerException("Unable to find Class for Extension Point '"+extPointName+"'.");
                }
            }
            else {
                extensionPointClass = Class.forName(className);
            }
            logger.info("Class for Extension Point '"+extPointName+"' successfully loaded.");
        } catch (ClassNotFoundException e) {
            logger.error("Unable to find Class for Extension Point '"+extPointName+"'.", e);
            throw new ExtensionManagerException("Unable to find Class for Extension Point '"+extPointName+"'.");
        }

        addExtensionPoint(extPointName, extensionPointClass, parameters);

        logger.info("Extension Point '"+extPointName+"' loaded.");
    }

    private void addExtensionPoint(String name, Class extensionPointClass, ExtensionPointParameters parameters)
            throws InitializationException, ExtensionManagerException
    {
        // URGENT cambiare il meccanismo per il controllo della superclass (instanciable?)
        Class superClass = extensionPointClass.getSuperclass();
        if (ExtensionPoint.class.equals(superClass)) {
            ExtensionPoint extPoint = null;
            try {
                extPoint = (ExtensionPoint) extensionPointClass.newInstance();
            } catch (InstantiationException e) {
                logger.error("Exception loading Extension Point '"+name+"'.", e);
                throw new ExtensionManagerException(e);
            } catch (IllegalAccessException e) {
                logger.error("Exception loading Extension Point '"+name+"'.", e);
                throw new ExtensionManagerException(e);
            }

            extPoint.setName(name);
            logger.info("Initializing Extension Point '"+name+"'...");
            try {
                extPoint.init(parameters);
            } catch (InitializationException e) {
                logger.error("Initialization Exception loading Extension Point '"+name+"'.", e);
                throw e;
            } catch (Throwable e) {
                logger.error("Exception loading Extension Point '"+name+"'.", e);
                throw new ExtensionManagerException(e);
            }

            logger.info("Done!");

            extensionPointsTable.put(name, extPoint);
            extensionPointsClassTable.put(extensionPointClass, extPoint);
            extensionPointsIdentiferTable.put(extPoint.getId(), extPoint);

        }
        else {
            logger.error("ExtensionPoint '"+name+"' is not an extension of the base class ExtensionPoint.");
        }
    }

    public void loadExtensions(String path) throws ExtensionManagerException {
        logger.info("Loading Extensions from dir '"+path+"'...");

        File rootDir = new File(path);
        if ((!rootDir.exists()) || (!rootDir.isDirectory())) {
            logger.error("Extensions directory '"+path+"' doesn't exists or is not a directory.");
            throw new ExtensionManagerException("Extensions directory '"+path+"' doesn't exists or is not a directory.");
        }

        File[] extDirs = rootDir.listFiles(directoryFilter);
        Arrays.sort(extDirs, extDirComparator);
        for (int i = 0; i < extDirs.length; i++) {
            String extensionName = getExtensionName(extDirs[i]);

            String extensionBasePath = extDirs[i].getPath();
            String extensionConfigFile = getConfigFileName(extDirs[i], extensionName);
            if (extensionConfigFile == null) {
                logger.error("Unable to find configuration file for extension '"+extensionName+"'");
                continue;
            }
            logger.info("Loading Extension '"+extensionName+"' ("+extensionBasePath+")...");
            try {
                addExtension(extensionName, extensionBasePath, extensionConfigFile);
            } catch (ExtensionManagerException e) {
                logger.error("Exception loading Extension '"+extensionName+"'.", e);
                continue;
            } catch (InitializationException e) {
                logger.error("Initialization Exception loading Extension '"+extensionName+"'.", e);
                continue;
            } catch (Throwable e) {
                logger.error("Exception loading Extension '"+extensionName+"'.", e);
                continue;
            }
            logger.info("Extension '"+extensionName+"' succesfully loaded.");
        }
    }

    private String getExtensionName(File extDir) {
        String dirname = extDir.getName();
        int sepIndex = dirname.indexOf('-');
        if (sepIndex < 0) {
            return dirname.trim();
        }
        else {
            int descSepIndex = dirname.indexOf('-', sepIndex+1);
            if (descSepIndex < 0) {
                String extName = dirname.substring(sepIndex+1, dirname.length()).trim();
                return extName;
            }
            else {
                String extName = dirname.substring(sepIndex+1, descSepIndex).trim();
                return extName;
            }
        }
    }

    private String getConfigFileName(File extDir, String extName) {
        File[] xmlFiles = extDir.listFiles(xmlFileFilter);
        if (xmlFiles.length == 0) {
            return null;
        }

        if (xmlFiles.length == 1) {
            return xmlFiles[0].getPath();
        }
        else {
            for (int i = 0; i < xmlFiles.length; i++) {
                String filename = xmlFiles[i].getName();
                if (filename.substring(0, filename.lastIndexOf('.')).equals(extName)) {
                    return xmlFiles[i].getPath();
                }
            }
            return xmlFiles[0].getPath();
        }
    }

    private void addExtension(String name, String path, String configFile)
            throws ExtensionManagerException, InitializationException {
        File confFile = new File(configFile);
        if (!confFile.exists()) {
            logger.error("Unable to find configuration file for extension '"+name+"'");
            throw new ExtensionManagerException("Unable to find configuration file for extension '"+name+"'");
        }

        Document configDoc = null;
        try {
            configDoc = xmlUtilities.getDocument(configFile);
        } catch (XMLUtilitiesException e) {
            logger.error("Unable to parse config file for extension '"+name+"'", e);
            throw new ExtensionManagerException("Unable to parse config file for extension '"+name+"'");
        }

        URL propertiesURL = null, classesURL = null, jarURL1 = null, jarURL2 = null;
        try {
            propertiesURL = new URL("file:" + path + "/properties/");
            classesURL = new URL("file:" + path + "/classes/");
            jarURL1 = new URL("file:" + path + "/" + name + ".jar");
            String confName = confFile.getName();
            int index = confName.lastIndexOf('.');
            confName = confName.substring(0, index);
            jarURL2 = new URL("file:" + path + "/" + confName + ".jar");
        } catch (MalformedURLException e) {
        }

        URL[] extensionClasspath;

        File libDir = new File(path + "/lib");
        if ((libDir.exists()) && (libDir.isDirectory())) {
            File[] libs = libDir.listFiles(jarFilter);
            extensionClasspath = new URL[libs.length+4];
            extensionClasspath[0] = propertiesURL;
            extensionClasspath[1] = classesURL;
            extensionClasspath[2] = jarURL1;
            extensionClasspath[3] = jarURL2;
            for (int i = 0; i < libs.length; i++) {
                try {
                    extensionClasspath[i+4] = new URL("file:" + path + "/lib/"+libs[i].getName());
                } catch (MalformedURLException e) {
                }
            }
        }
        else {
            extensionClasspath = new URL[] {
                    propertiesURL,
                    classesURL,
                    jarURL1,
                    jarURL2
            };
        }

        Element rootElement = configDoc.getDocumentElement();
        ClassLoader classLoader = null;

        NodeList dependsNodeList = rootElement.getElementsByTagName("Depends");
        if (dependsNodeList.getLength() > 0) {
            Element dependsElement = (Element) dependsNodeList.item(0);
            String depsExtesionName = dependsElement.getAttribute("name");
            ClassLoader parentClassLoader = (ClassLoader) extensionsClassloadersTable.get(depsExtesionName);
            if (parentClassLoader == null) {
                logger.error("Unable to find dependent extension '"+depsExtesionName+"' for extension '"+name+"'.");
                logger.error("Please check loading order of extensions.");
                throw new ExtensionManagerException("Unable to find dependent extension '"+depsExtesionName+"' for extension '"+name+"'.");
            }
            classLoader = new URLClassLoader(extensionClasspath, parentClassLoader);
        }
        else {
            classLoader = new URLClassLoader(extensionClasspath);
        }

        extensionsClassloadersTable.put(name, classLoader);

        NodeList extensionPoints = rootElement.getElementsByTagName("ExtensionPoint");
        int numOfExtPoints = extensionPoints.getLength();
        for (int i = 0; i < numOfExtPoints; i++) {
            Element extPointElement = (Element) extensionPoints.item(i);
            loadExtensionPoint(extPointElement, classLoader);
        }

        NodeList baseElementList = rootElement.getElementsByTagName("Extension");
        if (baseElementList.getLength() < 1) {
            logger.error("Configuration file for extension '"+name+"' doesn't contains an Extension definition.");
            throw new ExtensionManagerException("Configuration file for extension '"+name+"' doesn't contains an Extension definition.");
        }

        Element baseElement = (Element) baseElementList.item(0);
        String extensionPointName = baseElement.getAttribute("extensionPoint");
        String className = baseElement.getAttribute("className");

        ExtensionParameters parameters = getExtensionParameters(baseElement);

        ExtensionPoint extPoint = (ExtensionPoint) extensionPointsTable.get(extensionPointName);

        if (extPoint == null) {
            logger.error("Invalid Extension Point "+extensionPointName);
            throw new ExtensionManagerException("Invalid Extension Point "+extensionPointName);
        }

        try {
            logger.info("Loading Class for Extension '"+name+"'...");
            Class extensionClass = null;
            try {
                extensionClass = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                logger.error("Unable to find class '"+className+"' for Extension '"+name+"'.");
                throw new ExtensionManagerException("Unable to find class '"+className+"' for Extension '"+name+"'.");
            }
            logger.info("Class for Extension '"+name+"' successfully loaded.");

            logger.info("Checking Extension Point Interface...");
            Class extPointInterfaceClass = extPoint.getExtensionInterface();
            Class[] implementedInterfaces = extensionClass.getInterfaces();
            boolean found = false;
            for (int i = 0; i < implementedInterfaces.length; i++) {
                if (implementedInterfaces[i].equals(extPointInterfaceClass)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                logger.error("Extension Class for Extension '"+name+
                        "' doesn't implement Extension Point Interface '"+extPointInterfaceClass.getName()+"'");
                throw new ExtensionManagerException("Extension Class for Extension '"+name+
                        "' doesn't implement Extension Point Interface '"+extPointInterfaceClass.getName()+"'");
            }
            logger.info("Done!");

            Extension ext = (Extension) extensionClass.newInstance();
            logger.info("Initializing Extension "+name+"...");
            try {
                ext.init(parameters);
            } catch (InitializationException e) {
                throw e;
            }
            logger.info("done!");

            if (extensionsParametersWatchDogEnabled) {
                logger.info("Starting Extension Parameters Watch Dog for extension "+name+"...");
                ExtensionParametersWatchDog watchDog = new ExtensionParametersWatchDog(name, configFile, ext);
                watchDog.setReloadPeriod(watchDogReloadPeriod);
                watchDog.start();
                logger.info("done!");
            }

            extensionsTable.put(name, ext);
            extPoint.addExtension(ext);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public ExtensionParameters getExtensionParameters(Element baseElement) {
        NodeList parametersList = baseElement.getElementsByTagName("Parameter");
        int numOfParams = parametersList.getLength();
        ExtensionParameters parameters = new ExtensionParameters();
        for (int i = 0; i < numOfParams; i++) {
            Element paramElement = (Element) parametersList.item(i);
            String paramname = paramElement.getAttribute("name");
            String paramvalue = paramElement.getAttribute("value");
            parameters.addParameter(paramname, paramvalue);
        }
        return parameters;
    }

    private void _execute(ExtensionPoint extPoint) {
        Vector allExt = extPoint.getAllExtension();
        for (int i = 0; i < allExt.size(); i++) {
            Extension ext = (Extension) allExt.elementAt(i);
            extPoint.execute(ext);
        }
    }

    public void execute(String extensionPointName) {
        ExtensionPoint extPoint = (ExtensionPoint) extensionPointsTable.get(extensionPointName);
        if (extPoint == null) {
            logger.error("The Extesion Point '"+extensionPointName+"' doesn't exists.");
            return;
        }
        _execute(extPoint);
    }

    public void execute(Class extensionPointClass) {
        ExtensionPoint extPoint = (ExtensionPoint) extensionPointsClassTable.get(extensionPointClass);
        if (extPoint == null) {
            logger.error("The Extesion Point of class'"+extensionPointClass.getName()+"' doesn't exists.");
            return;
        }
        _execute(extPoint);
    }

    public void execute(ExtensionPointIdentifier extensionPointId) {
        ExtensionPoint extPoint = (ExtensionPoint) extensionPointsIdentiferTable.get(extensionPointId);
        if (extPoint == null) {
            logger.error("The Extesion Point with id '"+extensionPointId+"' doesn't exists.");
            return;
        }
        _execute(extPoint);
    }

    public ExtensionPoint getExtensionPoint(String extensionPointName) {
        ExtensionPoint extPoint = (ExtensionPoint) extensionPointsTable.get(extensionPointName);
        if (extPoint == null) {
            logger.error("The Extesion Point '"+extensionPointName+"' doesn't exists.");
            // IN PORGRESS exception???
            return null;
        }
        return extPoint;
    }

    public ExtensionPoint getExtensionPoint(Class extensionPointClass) {
        ExtensionPoint extPoint = (ExtensionPoint) extensionPointsClassTable.get(extensionPointClass);
        if (extPoint == null) {
            logger.error("The Extesion Point of class'"+extensionPointClass.getName()+"' doesn't exists.");
            // IN PORGRESS exception???
            return null;
        }
        return extPoint;
    }

    public ExtensionPoint getExtensionPoint(ExtensionPointIdentifier extensionPointId) {
        ExtensionPoint extPoint = (ExtensionPoint) extensionPointsIdentiferTable.get(extensionPointId);
        if (extPoint == null) {
            logger.error("The Extesion Point with id '"+extensionPointId+"' doesn't exists.");
            return null;
        }
        return extPoint;
    }

    public void shutdown() {
        {
            Set set = extensionsTable.entrySet();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Extension ext = (Extension) entry.getValue();
                ext.shutdown();
            }
        }

        {
            Set set = extensionPointsTable.entrySet();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ExtensionPoint extPoint = (ExtensionPoint) entry.getValue();
                extPoint.shutdown();
            }
        }
    }
}
