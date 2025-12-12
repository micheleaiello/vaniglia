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

import org.vaniglia.templateengine.ContextMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Vector;

public class GlobalParameters {

    private static GlobalParameters _instance;

    private ContextMap contextMap;
    private Vector modificationListeners;

    public static final GlobalParameters getInstance() {
        if (_instance == null) {
            _instance = new GlobalParameters();
        }

        return _instance;
    }

    private GlobalParameters() {
        this.contextMap = new ContextMap();
        this.modificationListeners = new Vector();
    }

    void loadGlobalParameters(Element rootElement) {
        ContextMap newMap = new ContextMap();
        NodeList globalParamList = rootElement.getElementsByTagName("Parameter");
        int numOfGlobalPar = globalParamList.getLength();
        for (int i = 0; i < numOfGlobalPar; i++) {
            Element globalParameterElement = (Element) globalParamList.item(i);
            String paramname = globalParameterElement.getAttribute("name");
            String paramvalue = globalParameterElement.getAttribute("value");
            newMap.put(paramname, paramvalue);
        }

        this.contextMap = newMap;

        GlobalParametersModificationListener[] currentListArray = (GlobalParametersModificationListener[]) modificationListeners.toArray(new GlobalParametersModificationListener[modificationListeners.size()]);
        for (int i = 0; i < currentListArray.length; i++) {
            currentListArray[i].globalParametersModified();
        }
    }

    public ContextMap getContextMap() {
        return contextMap;
    }

    public void addModificationsListener(GlobalParametersModificationListener listener) {
        modificationListeners.add(listener);
    }

    public void removeModificationsListener(GlobalParametersModificationListener listener) {
        modificationListeners.remove(listener);
    }
}
