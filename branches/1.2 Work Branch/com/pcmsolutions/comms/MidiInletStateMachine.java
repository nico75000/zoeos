package com.pcmsolutions.comms;

import com.pcmsolutions.system.IllegalStateTransitionException;
import com.pcmsolutions.system.StdStates;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Apr-2003
 * Time: 10:07:03
 * To change this template use Options | File Templates.
 */
public interface MidiInletStateMachine extends StdStates {
    public final static int STATE_REVOKED = 4;
    public final static int STATE_UNREVOKED = 5;
    public final static int STATE_DISCARDED = 6;
    public final static int[][] midiInletStateTransitions = new int[][]{
        /*STATE_PENDING*/       {STATE_INITIALIZED},
                                /*STATE_INITIALIZED*/   {STATE_INITIALIZED, STATE_STARTED, STATE_DISCARDED},
                                /*STATE_STARTED*/       {STATE_STARTED, STATE_STOPPED, STATE_REVOKED, STATE_DISCARDED},
                                /*STATE_STOPPED*/       {STATE_STOPPED, STATE_STARTED, STATE_REVOKED, STATE_DISCARDED},
                                /*STATE_REVOKED*/       {STATE_UNREVOKED, STATE_REVOKED},
                                /*STATE_UNREVOKED*/       {STATE_STARTED, STATE_STOPPED, STATE_REVOKED},
                                /*STATE_DISCARDED*/       {STATE_DISCARDED}};

    public final static String[] midiInletStateNames = new String[]{
        "STATE_PENDING", "STATE_INITIALIZED", "STATE_STARTED", "STATE_STOPPED", "STATE_REVOKED", "STATE_UNREVOKED", "STATE_DISCARDED"};

   // public void revoke() throws IllegalStateTransitionException;

   // public void unrevoke(Transmitter t) throws IllegalStateTransitionException;

    public void discard() throws IllegalStateTransitionException;
}
