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

import org.vaniglia.xml.XMLUtilities;
import org.vaniglia.xml.XMLUtilitiesException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Vector;

public class StateMachineConfigurator {

    public static StateMachine createStateMachine(String xmlConfigFile) throws StateMachineConfigurationException {
        // TODO schema checking
        XMLUtilities xmlUtils = new XMLUtilities();

        Document document = null;
        try {
            document = xmlUtils.getDocument(xmlConfigFile);
        } catch (XMLUtilitiesException e) {
            throw new StateMachineConfigurationException("Unable to load configuration form file "+xmlConfigFile, e);
        }

        Element rootElement = document.getDocumentElement();

        StateFactory stateFactory = StateFactory.getInstance();

        HashMap stateMap = new HashMap();
        Vector states = new Vector();

        NodeList statesList = rootElement.getElementsByTagName("State");
        int numOfStates = statesList.getLength();
        for (int i = 0; i < numOfStates; i++) {
            Element stateElement = (Element) statesList.item(i);
            String stateName = stateElement.getAttribute("name");
            String stateDesc = stateElement.getAttribute("description");

            State state = stateFactory.createState(stateName, stateDesc);
            stateMap.put(stateName, state);
            states.addElement(state);
        }

        EventFactory eventFactory = EventFactory.getInstance();

        HashMap eventMap = new HashMap();
        Vector events = new Vector();

        NodeList eventsList = rootElement.getElementsByTagName("Event");
        int numOfEvents = eventsList.getLength();
        for (int i = 0; i < numOfEvents; i++) {
            Element eventElement = (Element) eventsList.item(i);
            String eventName = eventElement.getAttribute("name");
            String eventDesc = eventElement.getAttribute("desc");

            Event event = eventFactory.createEvent(eventName, eventDesc);
            eventMap.put(eventName, event);
            events.addElement(event);
        }

        State[] statesArray = new State[0];
        Event[] eventsArray = new Event[0];

        statesArray = (State[]) states.toArray(statesArray);
        eventsArray = (Event[]) events.toArray(eventsArray);


        StateMachine stateMachine = new StateMachine(statesArray, eventsArray);

        NodeList transitionsList = rootElement.getElementsByTagName("Transition");
        int numOfTransitions = transitionsList.getLength();
        for (int i = 0; i < numOfTransitions; i++) {
            Element transitionElement = (Element) transitionsList.item(i);
            String fromStateName = transitionElement.getAttribute("fromState");
            String eventName = transitionElement.getAttribute("event");
            String toStateName = transitionElement.getAttribute("toState");

            Event event = (Event) eventMap.get(eventName);
            State fromState = (State) stateMap.get(fromStateName);
            State toState = (State) stateMap.get(toStateName);

            try {
                stateMachine.setTransition(fromState, event, toState);
            } catch (StateMachineException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                continue;
            }

            NodeList actionList = transitionElement.getElementsByTagName("Action");
            int numOfActions = actionList.getLength();

            for (int k = 0; k < numOfActions; k++) {
                Element actionElement = (Element) actionList.item(k);
                String className = actionElement.getAttribute("class");
                Action action = null;
                try {
                    Class actionClass = Class.forName(className);
                    action = (Action) actionClass.newInstance();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                } catch (InstantiationException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }

                action.configure(actionElement);

                try {
                    stateMachine.addAction(fromState,  toState, action);
                } catch (StateMachineException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                }
            }

        }

        return stateMachine;
    }


}
