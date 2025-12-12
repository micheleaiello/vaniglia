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
package org.vaniglia.statemachine;

import org.w3c.dom.Element;

/**
 * Basic Interface for actions.
 */
public interface Action {

    /**
     * This method is used to configure the action.
     * It takes as input parameter an XML Element.
     * Each user-defined Action class can define the schema of the XML needed for the configuration.
     *
     * @param rootElement
     */
    public abstract void configure(Element rootElement);

    public abstract void execute(State inputState,
                                 State  outputState,
                                 ObjectWithState inputObject);

    public abstract void execute(State inputState,
                                 State  outputState,
                                 ObjectWithState inputObject,
                                 ActionParameter parameter);

}
