package com.pcmsolutions.system;

import com.pcmsolutions.system.tasking.SendableTicket;
import com.pcmsolutions.system.tasking.SyncTicket;
import com.pcmsolutions.device.EMU.DeviceException;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 13:44:25
 * To change this template use Options | File Templates.
 */
public interface ZExternalDevice extends LicensedEntity, ZDisposable/*,Criticality*/ {
    public final static int STATE_PENDING = 0;
    public final static int STATE_RUNNING = 2;
    public final static int STATE_STOPPED = 3;
    public final static int STATE_REMOVED = 4;
    public final static int STATE_MARKED_DUPLICATE = 5;
    public final static String[] stateNames = new String[]{
        "STATE_PENDING", "", "STATE_RUNNING", "STATE_STOPPED", "STATE_REMOVED", "STATE_MARKED_DUPLICATE"};
    public final static int[][] stateTransitions = new int[][]{
        /*STATE_PENDING*/       {STATE_RUNNING, STATE_MARKED_DUPLICATE, STATE_REMOVED},
                                {1},
                                /*STATE_RUNNING*/       {STATE_RUNNING, STATE_STOPPED},
                                /*STATE_STOPPED*/       {STATE_STOPPED, STATE_RUNNING, STATE_REMOVED},
                                /*STATE_REMOVED*/       {STATE_REMOVED},
                                /*STATE_MARKED_DUPLICATE*/ {STATE_MARKED_DUPLICATE}};

    public interface TransitionMonitor {
            TransitionMonitor TRUE = new TransitionMonitor() {
                public boolean hasTransitioned() {
                    return true;
                }
            };

            boolean hasTransitioned();
        }

    // should be capable of handling retries
    public SyncTicket startDevice() throws ZDeviceStartupException, IllegalStateTransitionException;

    public void stopDevice(boolean waitForConfigurers, String reason) throws IllegalStateTransitionException;

    public void removeDevice(boolean saveState) throws ZDeviceCannotBeRemovedException, IllegalStateTransitionException;

    public Object getDeviceIdentityMessage();

    public SystemEntryPoint getSystemEntryPoint();

    public String getDeviceCategory();

    public String getDeviceConfigReport() ;

    public int getState();

    public String getReasonForState();

    public String getStaticName();

    public String getName();

    public void setName(String name) throws DeviceException;
}
