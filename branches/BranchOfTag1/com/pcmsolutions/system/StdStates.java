package com.pcmsolutions.system;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Apr-2003
 * Time: 10:09:12
 * To change this template use Options | File Templates.
 */
public interface StdStates {
    public final static int STATE_PENDING = 0;
    public final static int STATE_INITIALIZED = 1;
    public final static int STATE_STARTED = 2;
    public final static int STATE_STOPPED = 3;

    public final static int[][] stdStateTransitions = new int[][]{
        /*STATE_PENDING*/       {STATE_INITIALIZED},
                                /*STATE_INITIALIZED*/   {STATE_INITIALIZED, STATE_STARTED},
                                /*STATE_STARTED*/       {STATE_STARTED, STATE_STOPPED},
                                /*STATE_STOPPED*/       {STATE_STOPPED, STATE_STARTED}};

    public final static String[] stdStateNames = new String[]{
        "STATE_PENDING", "STATE_INITIALIZED", "STATE_STARTED", "STATE_STOPPED"};
}
