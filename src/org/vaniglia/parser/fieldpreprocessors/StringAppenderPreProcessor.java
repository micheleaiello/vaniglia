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

public class StringAppenderPreProcessor implements FieldPreProcessor {

    private String prefix;
    private String suffix;

    public StringAppenderPreProcessor() {
    }

    public StringAppenderPreProcessor(String prefix, String suffix) {
        if ((prefix != null) && (!prefix.equals(""))) {
            this.prefix = prefix;
        }
        else {
            this.prefix = "";
        }

        if ((suffix != null) && (!suffix.equals(""))) {
            this.suffix = suffix;
        }
        else {
            this.suffix = "";
        }
    }

    public void configure(Element rootElement) {
        String prefix = rootElement.getAttribute("prefix");
        String suffix = rootElement.getAttribute("suffix");

        if ((prefix != null) && (!prefix.equals(""))) {
            this.prefix = prefix;
        }
        else {
            this.prefix = "";
        }

        if ((suffix != null) && (!suffix.equals(""))) {
            this.suffix = suffix;
        }
        else {
            this.suffix = "";
        }
    }

    public String process(String value) throws FieldPreProcessorException {
        if (value != null) {
            return this.prefix+value+this.suffix;
        }
        else {
            return this.prefix+this.suffix;
        }
    }
}
