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

public class CharsetValidator implements Validator {

    private char[] charset;
    private boolean in;

    public CharsetValidator() {
        this.charset = new char[0];
        this.in = true;
    }

    public CharsetValidator(char[] charset, boolean in) {
        this.charset = charset;
        this.in = in;
    }

    public char[] getCharset() {
        return charset;
    }

    public void setCharset(char[] charset) {
        this.charset = charset;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public void configure(Element rootElement) {
        String charsetStr = rootElement.getAttribute("charset");
        String inStr = rootElement.getAttribute("in");

        this.charset = new char[charsetStr.length()];
        for (int i = 0; i < charsetStr.length(); i++) {
            this.charset[i] = charsetStr.charAt(i);
        }

        try {
            this.in = Boolean.valueOf(inStr).booleanValue();
        } catch (Exception e) {
            this.in = true;
        }
    }

    public boolean validate(Object obj) throws ValidatorException {
        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (in) {
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (!isInCharset(c)) {
                        return false;
                    }
                }
                return true;
            }
            else {
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (isInCharset(c)) {
                        return false;
                    }
                }
                return true;
            }
        }
        else if (obj instanceof Character) {
            Character character = (Character) obj;
            char c = character.charValue();
            for (int k = 0; k < charset.length; k++) {
                if (c == charset[k]) {
                    return in;
                }
            }
            return !in;
        }
        else {
            throw new ValidatorException("Charset validation can't be applied to object of class "+obj.getClass().getName());
        }
    }

    private boolean isInCharset(char c) {
        for (int k = 0; k < charset.length; k++) {
            if (c == charset[k]) {
                return true;
            }
        }
        return false;
    }
}
