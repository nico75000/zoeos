package com.pcmsolutions.system;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-Apr-2003
 * Time: 09:14:41
 * To change this template use Options | File Templates.
 */
public interface StandardStateMachine extends StdStates {

    public void stateInitial() throws IllegalStateTransitionException;

    public void stateStart() throws IllegalStateTransitionException;

    public void stateStop() throws IllegalStateTransitionException;

    public int getState();
}
