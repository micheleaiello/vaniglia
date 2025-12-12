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
import java.util.Timer;
import java.util.TimerTask;

public class ExtensionParametersWatchDog implements GlobalParametersModificationListener {

    private static final Logger logger = Logger.getLogger(ExtensionParametersWatchDog.class);

    private static final int DEFAULT_RELOAD_PERIOD = 10000;

    private String extensionName;
    private String configFileName;
    private File configFile;
    private long lastModified;
    private Extension extension;
    private XMLUtilities xmlUtilities;
    private Timer timer;

    private int reloadPeriod = DEFAULT_RELOAD_PERIOD;

    public ExtensionParametersWatchDog(String extensionName, String configFileName, Extension extension) {
        this.extensionName = extensionName;
        this.configFileName = configFileName;
        this.configFile = new File(configFileName);
        this.lastModified = configFile.lastModified();
        this.extension = extension;
        this.xmlUtilities = new XMLUtilities();
    }


    public int getReloadPeriod() {
        return reloadPeriod;
    }

    public void setReloadPeriod(int reloadPeriod) {
        this.reloadPeriod = reloadPeriod;
        if (timer != null) {
            stop();
            start();
        }
    }

    public void start() {
        GlobalParameters.getInstance().addModificationsListener(this);

        final ExtensionParametersWatchDog watchDog = this;

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                watchDog.checkFile();
            }
        }, reloadPeriod, reloadPeriod);
    }

    public void stop() {
        GlobalParameters.getInstance().removeModificationsListener(this);

        timer.cancel();
    }

    private void checkFile() {
        long mod = configFile.lastModified();
        if (mod > lastModified) {
            lastModified = mod;
            reloadParameters();
        }
    }

    public void globalParametersModified() {
        reloadParameters();
    }

    private synchronized void reloadParameters() {
        Document configDoc = null;
        try {
            configDoc = xmlUtilities.getDocument(configFileName);
        } catch (XMLUtilitiesException e) {
            logger.error("Unable to parse config file '"+configFileName+"'", e);
            return;
        }
        Element rootElement = configDoc.getDocumentElement();

        NodeList baseElementList = rootElement.getElementsByTagName("Extension");
        if (baseElementList.getLength() < 1) {
            logger.error("Configuration file '"+configFileName+"' doesn't contains an Extension definition.");
            return;
        }

        Element baseElement = (Element) baseElementList.item(0);
        ExtensionParameters parameters = ExtensionManager.getInstance().getExtensionParameters(baseElement);

        try {
            extension.reloadParameters(parameters);
        } catch (ReloadNotSupportedException e) {
            logger.error("Extension "+extensionName+" doesn't support reload of parameters. " +
                    "Stopping Extension Parameters Watch Dog.");
            stop();
        }
    }
}
