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
package org.vaniglia.statemachine.actions;

import org.vaniglia.statemachine.Action;
import org.vaniglia.statemachine.ActionParameter;
import org.vaniglia.statemachine.ObjectWithState;
import org.vaniglia.statemachine.State;
import org.vaniglia.statemachine.parameters.StringBufferParameter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LoggingAction implements Action {

    private String loggingPrefix = "LOGGINGACTION";

    public void configure(Element rootElement) {
        NodeList logList = rootElement.getElementsByTagName("LoggingPrefix");
        if (logList.getLength() > 0) {
            Element loggingPrefixElement = (Element) logList.item(0);
            loggingPrefix = loggingPrefixElement.getAttribute("value");
        }
    }

    public void execute(State inputState, State outputState, ObjectWithState inputObject) {
        System.out.println(loggingPrefix+" - From "+inputState.getName()+" to "+outputState.getName());
    }

    public void execute(State inputState, State outputState, ObjectWithState inputObject, ActionParameter parameter) {
        if (parameter instanceof StringBufferParameter) {
            StringBufferParameter param = (StringBufferParameter) parameter;
            param.append(loggingPrefix+" - From "+inputState.getName()+" to "+outputState.getName());
            param.append(System.getProperty("line.separator"));
        }
        else {
            execute(inputState, outputState, inputObject);
        }
    }

}
