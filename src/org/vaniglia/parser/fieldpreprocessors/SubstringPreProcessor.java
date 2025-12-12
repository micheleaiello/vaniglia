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

public class SubstringPreProcessor implements FieldPreProcessor {

    private int startIndex;
    private int endIndex;

    public SubstringPreProcessor() {
    }

    public SubstringPreProcessor(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        if (this.startIndex < 0) this.startIndex = 0;

        this.endIndex = endIndex;
    }

    public void configure(Element rootElement) {
        startIndex = 0;
        endIndex = -1;

        String startIndexStr = rootElement.getAttribute("start");
        String endIndexStr = rootElement.getAttribute("end");

        if (!startIndexStr.equals("")) {
            try {
                startIndex = Integer.parseInt(startIndexStr);
            } catch (NumberFormatException e) {
                startIndex = 0;
            }
        }

        if (!endIndexStr.equals("")) {
            try {
                endIndex = Integer.parseInt(endIndexStr);
            } catch (NumberFormatException e) {
                endIndex = -1;
            }
        }

    }

    public String process(String value) throws FieldPreProcessorException {
        if (value == null) return null;

        return value.substring(Math.min(startIndex, value.length()), Math.min(endIndex<0?value.length():endIndex, value.length()));
    }
}
