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

import org.vaniglia.templateengine.TemplateEngine;

import java.util.HashMap;

public class ExtensionParameters {

    private HashMap params;

    ExtensionParameters() {
        this.params = new HashMap();
    }

    void addParameter(String name, String value) {
        params.put(name, value);
    }

    public String getParameter(String name) {
        String value = (String) params.get(name);
        if (value != null) {
            return TemplateEngine.merge(value, GlobalParameters.getInstance().getContextMap());
        }
        else {
            return null;
        }
    }

    public String getStringParameter(String name, String defaultValue) {
        String value = (String) params.get(name);
        if (value != null) {
            return TemplateEngine.merge(value, GlobalParameters.getInstance().getContextMap());
        }
        else {
            return defaultValue;
        }
    }

    // TODO template engine also for the other types of parameters?
    // TODO template engine also for the default value?
    public int getIntegerParameter(String name, int defaultValue) {
        String strValue = getParameter(name);
        if (strValue == null) {
            return defaultValue;
        }

        int intValue = 0;
        try {
            intValue = Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            intValue = defaultValue;
        }

        return intValue;
    }

    public long getLongParameter(String name, long defaultValue) {
        String strValue = getParameter(name);
        if (strValue == null) {
            return defaultValue;
        }

        long longValue = 0;
        try {
            longValue = Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            longValue = defaultValue;
        }

        return longValue;
    }

    public float getFloatParameter(String name, float defaultValue) {
        String strValue = getParameter(name);
        if (strValue == null) {
            return defaultValue;
        }

        float floatValue = 0;
        try {
            floatValue = Float.parseFloat(strValue);
        } catch (NumberFormatException e) {
            floatValue = defaultValue;
        }

        return floatValue;
    }

    public double getDoubleParameter(String name, double defaultValue) {
        String strValue = getParameter(name);
        if (strValue == null) {
            return defaultValue;
        }

        double doubleValue = 0;
        try {
            doubleValue = Double.parseDouble(strValue);
        } catch (NumberFormatException e) {
            doubleValue = defaultValue;
        }

        return doubleValue;
    }

    public boolean getBooleanParameter(String name, boolean defaultValue) {
        String strValue = getParameter(name);
        if (strValue == null) {
            return defaultValue;
        }

        return Boolean.valueOf(strValue).booleanValue();
    }

    public char getCharParameter(String name, char defaultValue) {
        String strValue = getParameter(name);
        if (strValue == null) {
            return defaultValue;
        }

        return strValue.charAt(0);
    }
    
}
