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

package org.vaniglia.parser.fieldpreprocessors;

import org.vaniglia.parser.FieldPreProcessor;
import org.vaniglia.parser.FieldPreProcessorException;
import org.w3c.dom.Element;

public class TrimPreProcessor implements FieldPreProcessor {

    private char[] trimLeadingChars;
    private char[] trimTrailingChars;

    public TrimPreProcessor() {
    }

    public TrimPreProcessor(char[] trimLeadingChars, char[] trimTrailingChars) {
        this.trimLeadingChars = trimLeadingChars;
        this.trimTrailingChars = trimTrailingChars;
    }

    public void configure(Element rootElement) {
        String leadingCharsStr = rootElement.getAttribute("trimLeading");
        String trailingCharsStr = rootElement.getAttribute("trimTrailing");

        if ((leadingCharsStr != null) && (!leadingCharsStr.equals(""))) {
            trimLeadingChars = new char[leadingCharsStr.length()];
            leadingCharsStr.getChars(0, leadingCharsStr.length(), trimLeadingChars, 0);
        }

        if ((trailingCharsStr != null) && (!trailingCharsStr.equals(""))) {
            trimTrailingChars = new char[trailingCharsStr.length()];
            trailingCharsStr.getChars(0, trailingCharsStr.length(), trimTrailingChars, 0);
        }
    }

    public String process(String value) throws FieldPreProcessorException {
        if (value == null) return null;

        int startIndex = 0;
        int endIndex = value.length()-1;

        if ((trimLeadingChars != null) && (trimLeadingChars.length > 0)) {
            while ((startIndex < value.length()) && contains(value.charAt(startIndex), trimLeadingChars)) {
                startIndex++;
            }
        }

        if ((trimTrailingChars!= null) && (trimTrailingChars.length > 0)) {
            while ((endIndex >= startIndex) && contains(value.charAt(endIndex), trimTrailingChars)) {
                endIndex--;
            }
        }

        if (endIndex >= startIndex) {
            return value.substring(startIndex, endIndex+1);
        }
        else {
            return "";
        }
    }

    private boolean contains(char c, char[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == c) return true;
        }

        return false;
    }
}
