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
package org.vaniglia.examples.statemachine;

import org.vaniglia.statemachine.*;
import org.vaniglia.statemachine.parameters.StringBufferParameter;

public class Main {

    public static void main(String[] args) throws StateMachineConfigurationException, StateMachineException {
        StateFactory stateFactory = StateFactory.getInstance();
        EventFactory eventFactory = EventFactory.getInstance();

//        State stateOne = stateFactory.createState("One", "State One");
//        State stateTwo = stateFactory.createState("Two", "State Two");
//        State stateThree  = stateFactory.createState("Three", "State Three");
//
//        State[] states1 = {stateOne, stateThree, stateTwo};
//
//        Event eventOne = eventFactory.createEvent("E-One", "Event One");
//
//        Event[] events1 = {eventOne};
//
//        StateMachine stateMachine1 = new StateMachine(states1, events1);
//
//        stateMachine1.setAction(stateOne, stateTwo, new LoggingAction());
//        stateMachine1.setAction(stateTwo, stateThree, new LoggingAction());
//        stateMachine1.setAction(stateThree, stateOne, new LoggingAction());
//
//        stateMachine1.setTransition(stateOne, eventOne, stateTwo);
//        stateMachine1.setTransition(stateTwo, eventOne, stateThree);
//        stateMachine1.setTransition(stateThree, eventOne, stateOne);

        StateMachine stateMachine1 = StateMachineConfigurator.createStateMachine("xml/statemachine/StateMachine.xml");

        State stateOne = stateFactory.getStateByName("One");
        Event eventOne = eventFactory.getEventByName("E-One");

        MyObject obj = new MyObject();
        obj.setState(stateOne);
        System.out.println("Initial State = "+obj.getState().getName());

        stateMachine1.handleEvent(obj, eventOne);
        stateMachine1.handleEvent(obj, eventOne);
        stateMachine1.handleEvent(obj, eventOne);
        stateMachine1.handleEvent(obj, eventOne);
        stateMachine1.handleEvent(obj, eventOne);
        stateMachine1.handleEvent(obj, eventOne);
        stateMachine1.handleEvent(obj, eventOne);

        State finalState = obj.getState();
        System.out.println("Final State = "+finalState.getName());

        System.out.println("Now with the parameter");
        StringBufferParameter param = new StringBufferParameter(100);

        stateMachine1.handleEvent(obj, eventOne, param);
        stateMachine1.handleEvent(obj, eventOne, param);
        stateMachine1.handleEvent(obj, eventOne, param);
        stateMachine1.handleEvent(obj, eventOne, param);
        stateMachine1.handleEvent(obj, eventOne, param);
        stateMachine1.handleEvent(obj, eventOne, param);
        stateMachine1.handleEvent(obj, eventOne, param);

        finalState = obj.getState();
        System.out.println("Final State = "+finalState.getName());

        System.out.println("-- Parameter Content --");
        System.out.println(param.toString());
        System.out.println("-- Parameter Content --");

    }

}
