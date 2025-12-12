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

public class NotNullValidator implements Validator {

    public void configure(Element rootElement) {
    }

    public boolean validate(Object obj) throws ValidatorException {
        if (obj == null) {
            return false;
        }
        else {
            return (obj.toString().length() > 0);
        }
    }
}
