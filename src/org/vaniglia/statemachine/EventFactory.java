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

public class EventFactory {

    private int nextID = 0;
    private Vector allEvents = new Vector();
    private HashMap eventsByName = new HashMap();

    private static EventFactory _instance;

    private EventFactory() {
    }

    public static synchronized final EventFactory getInstance() {
        if (_instance == null) {
            _instance = new EventFactory();
        }
        return _instance;
    }

    public Event createEvent(String name, String description) {
        Event event = new Event(name, description, nextID++);
        allEvents.addElement(event);
        eventsByName.put(name, event);
        return event;
    }

    public Event getState(int id) {
        return (Event) allEvents.get(id);
    }

    public Event getEventByName(String name) {
        return (Event) eventsByName.get(name);
    }

}
