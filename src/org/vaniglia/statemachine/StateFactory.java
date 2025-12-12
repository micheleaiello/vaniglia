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

import java.util.HashMap;
import java.util.Vector;

public class StateFactory {

    private int nextID = 0;
    private Vector allStates = new Vector();
    private HashMap statesByName = new HashMap();

    private static StateFactory _instance;

    private StateFactory() {
    }

    public static synchronized final StateFactory getInstance() {
        if (_instance == null) {
            _instance = new StateFactory();
        }
        return _instance;
    }

    public State createState(String name, String description) {
        State state = new State(name, description, nextID++);
        allStates.addElement(state);
        statesByName.put(name, state);
        return state;
    }

    public State getState(int id) {
        return (State) allStates.get(id);
    }

    public State getStateByName(String name) {
        return (State) statesByName.get(name);
    }
}
