package com.pcmsolutions.system;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 06-Apr-2003
 * Time: 09:31:12
 * To change this template use Options | File Templates.
 */
public class StateMachineHelper implements StdStates, Serializable {
    private int[][] transitions;
    private volatile int currState;
    private String[] stateNames;

    public StateMachineHelper() {
        this(StdStates.STATE_PENDING, StdStates.stdStateTransitions, StdStates.stdStateNames);
    }

    public StateMachineHelper(int initState, int[][] transitions, String[] stateNames) {
        this.currState = initState;
        this.transitions = transitions;
        this.stateNames = stateNames;

        // do some checks first to make sure we have somewhat valid data
        try {
            int[] t = transitions[currState];
            String s = stateNames[currState];
            s = stateNames[transitions.length - 1];
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid initial state in State Transition System");
        }
    }

    public synchronized int getState() {
        return currState;
    }

    public synchronized String getCurrentStateName() {
        return stateNames[currState];
    }

    public synchronized String getStateName(int state) {
        try {
            return stateNames[state];
        } catch (Exception e) {

        }
        return "";
    }

    public synchronized int testTransition(int newState) throws IllegalStateTransitionException {
        if (isLegal(transitions[currState], newState) == false)
            try {
                throw new IllegalStateTransitionException(stateNames[currState], stateNames[newState], null);
            } catch (Exception e1) {
                throw new IllegalStateTransitionException(stateNames[currState], "Unknown State", null);
            }
        return currState;
    }

    public synchronized int transition(int newState) throws IllegalStateTransitionException {
        int oldState = testTransition(newState);
        currState = newState;
        return oldState;
    }

    private boolean isLegal(int[] t, int state) {
        for (int n = 0; n < t.length; n++)
            if (t[n] == state)
                return true;
        return false;
    }

}
