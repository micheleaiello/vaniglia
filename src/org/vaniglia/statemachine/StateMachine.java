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

import java.util.Arrays;

public class StateMachine {

    /**
     * This is the array of states for the StateMachine.
     */
    private State[] states;

    /**
     * This is the array of events for the StateMachien.
     */
    private Event[] events;

    /**
     * This is a matrix representing the action to execute if an object changes state.
     * On the first dimensions there are the source state ids, and on the second the destination state ids.
     */
    private ActionList[][] actionMatrix;

    /**
     * This is a matrix used to find the destination state given the source state and the event.
     * On the first dimension there are the source state ides, and on the second the event ids.
     */
    private int[][] transactionMatrix;

    /**
     * This is an array used to normalize the states ids.
     */
    private int[] stateIDTraslator;

    /**
     * This is an array used to normalize the events ids.
     */
    private int[] eventIDTraslator;


    public StateMachine(State[] states, Event[] events) {
        this.states = new State[states.length];
        System.arraycopy(states, 0, this.states, 0, states.length);

        this.stateIDTraslator = new int[states.length];

        for (int i = 0; i < this.states.length; i++) {
            int id = this.states[i].getId();
            if (id >= stateIDTraslator.length) {
                int[] newArray = new int[id+1];
                System.arraycopy(stateIDTraslator, 0, newArray, 0, stateIDTraslator.length);
                stateIDTraslator = newArray;
            }
            stateIDTraslator[id] = i;
        }

        this.actionMatrix = new ActionList[this.states.length][this.states.length];

        this.events = new Event[events.length];
        System.arraycopy(events, 0, this.events, 0, events.length);

        this.eventIDTraslator = new int[events.length];

        for (int i = 0; i < this.events.length; i++) {
            int id = this.events[i].getId();
            if (id >= eventIDTraslator.length) {
                int[] newArray = new int[id+1];
                System.arraycopy(eventIDTraslator, 0, newArray, 0, eventIDTraslator.length);
                eventIDTraslator = newArray;
            }
            eventIDTraslator[id] = i;
        }

        this.transactionMatrix = new int[this.states.length][this.events.length];
        for (int i = 0; i < this.states.length; i++) {
            Arrays.fill(this.transactionMatrix[i], -1);
        }
    }

    public void addAction(State inputState, State outputState, Action action) throws StateMachineException {
        int inputId = inputState.getId();
        int outputId = outputState.getId();

        if ((inputId >= stateIDTraslator.length) || (outputId >= stateIDTraslator.length)) {
            throw new StateMachineException("Invalid state");
        }
        ActionList list = actionMatrix[stateIDTraslator[inputId]][stateIDTraslator[outputId]];
        if (list == null) {
            list = new ActionList();
            actionMatrix[stateIDTraslator[inputId]][stateIDTraslator[outputId]] = list;
        }
        list.add(action);
    }

    public void setTransition(State inputState, Event inputEvent, State outputState) throws StateMachineException {
        int inputStateId = inputState.getId();
        int inputEventId = inputEvent.getId();
        int outputStateId = outputState.getId();

        if ((inputStateId >= stateIDTraslator.length) || (outputStateId >= stateIDTraslator.length)) {
            throw new StateMachineException("Invalid state");
        }

        if (inputEventId >= eventIDTraslator.length) {
            throw new StateMachineException("Invalid event");
        }

        transactionMatrix[stateIDTraslator[inputStateId]][eventIDTraslator[inputEventId]] = outputStateId;
    }

    public void handleEvent(ObjectWithState object, Event event) throws StateMachineException {
        this.handleEvent(object, event, null);
    }

    public void handleEvent(ObjectWithState object, Event event, ActionParameter param) throws StateMachineException {
        State inputState = object.getState();
        if (inputState == null) {
            throw new StateMachineException("Object doesn't have a state assigned.");
        }

        int inputStateId = inputState.getId();
        int inputEventId = event.getId();

        if (inputStateId >= stateIDTraslator.length) {
            throw new StateMachineException("Invalid State");
        }

        if (inputEventId >= eventIDTraslator.length) {
            throw new StateMachineException("Invalid Event");
        }

        int outputStateId = transactionMatrix[stateIDTraslator[inputStateId]][eventIDTraslator[inputEventId]];
        if (outputStateId >= 0) {
            State outputState = states[stateIDTraslator[outputStateId]];
            ActionList actionList = actionMatrix[stateIDTraslator[inputStateId]][stateIDTraslator[outputStateId]];

            if (actionList != null) {
                for (int i = 0; i < actionList.size(); i++) {
                    Action action = actionList.get(i);
                    if (param != null) {
                        action.execute(inputState, outputState, object, param);
                    }
                    else {
                        action.execute(inputState, outputState, object);
                    }
                }
            }

            object.setState(outputState);

        }

    }


}
