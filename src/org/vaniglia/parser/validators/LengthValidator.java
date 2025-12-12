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
package org.vaniglia.parser.validators;

import org.vaniglia.parser.Validator;
import org.vaniglia.parser.ValidatorException;
import org.w3c.dom.Element;

public class LengthValidator implements Validator {

    private int minLength;
    private int maxLength;

    public LengthValidator() {
        minLength = -1;
        maxLength = -1;
    }

    public LengthValidator(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void configure(Element rootElement) {
        String minLengthStr = rootElement.getAttribute("minLength");
        String maxLengthStr = rootElement.getAttribute("maxLength");

        if (!minLengthStr.equals("")) {
            try {
                minLength = Integer.parseInt(minLengthStr);
            } catch (NumberFormatException e) {
                minLength = -1;
            }
        }

        if (!maxLengthStr.equals("")) {
            try {
                maxLength = Integer.parseInt(maxLengthStr);
            } catch (NumberFormatException e) {
                maxLength = -1;
            }
        }
    }

    public boolean validate(Object obj) throws ValidatorException {
        boolean valid = true;
        if (obj == null) {
            return (minLength < 0);
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if ((minLength >= 0) && (str.length() < minLength)) {
                valid = false;
            }
            if ((maxLength >= 0) && (str.length() > maxLength)) {
                valid = false;
            }
        }
        else {
            throw new ValidatorException("Length validation can't be applied to object of class "+obj.getClass().getName());
        }

        return valid;
    }

}
