package com.pcmsolutions.device.EMU.E4;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 17-Apr-2003
 * Time: 20:26:38
 * To change this template use Options | File Templates.
 */
public interface RemoteObjectStates {
    public static final int STATE_PENDING = 0;
    public static final int STATE_NAMED = 1;
    public static final int STATE_INITIALIZED = 2;
    public static final int STATE_EMPTY = 3;
    public static final int STATE_INITIALIZING = 4;

    // used by initialization monitors
    public static final int STATUS_INITIALIZED = Integer.MIN_VALUE;
}
