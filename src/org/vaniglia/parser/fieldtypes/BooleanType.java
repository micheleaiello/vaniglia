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
package org.vaniglia.parser.fieldtypes;

import org.vaniglia.parser.FieldType;
import org.w3c.dom.Element;

public class BooleanType extends FieldType {

    private boolean trim;
    private boolean ignoreCase;
    private String trueValue;

    public BooleanType() {
        this.trim = false;
    }

    public BooleanType(boolean trim) {
        this.trim = trim;
    }

    public void configure(Element rootElement) {
        String trimStr = rootElement.getAttribute("trim");
        if (trimStr.length() > 0) {
            this.trim = Boolean.valueOf(trimStr).booleanValue();
        }

        String ignoreStr = rootElement.getAttribute("ignoreCase");
        if (ignoreStr.length() > 0) {
            this.ignoreCase = Boolean.valueOf(ignoreStr).booleanValue();
        }

        trueValue = rootElement.getAttribute("trueValue");
    }

    protected Object _createField(String value) {
        if (value == null) {
            return Boolean.FALSE;
        }
        else {
            String v = value;
            if (trim) {
                v = value.trim();
            }

            if (ignoreCase) {
                return new Boolean(v.equalsIgnoreCase(trueValue));
            }
            else {
                return new Boolean(v.equals(trueValue));
            }
        }
    }

}
