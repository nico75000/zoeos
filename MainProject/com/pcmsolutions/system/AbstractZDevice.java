package com.pcmsolutions.system;

import com.pcmsolutions.system.tasking.SyncTicket;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Apr-2003
 * Time: 06:51:17
 * To change this template use Options | File Templates.
 */
public abstract class AbstractZDevice implements ZExternalDevice, Serializable {
    // STATE
    //private transient CriticalityMonitor criticality = new CriticalityMonitor();

    protected StdStateMachineHelper sts = new StdStateMachineHelper(STATE_PENDING, stateTransitions, stateNames);

    protected Object identityMessage;

    public SystemEntryPoint getSystemEntryPoint() {
        return new Impl_SystemEntryPoint(getClass(), getDeviceIdentityMessage().toString());
    }

    public AbstractZDevice(Object identityMessage) {
        this.identityMessage = identityMessage;
    }

    public abstract SyncTicket startDevice() throws ZDeviceStartupException, IllegalStateTransitionException;

    public abstract void stopDevice(boolean waitForConfigurers, String reason) throws IllegalStateTransitionException;

    public abstract void removeDevice(boolean saveState) throws ZDeviceCannotBeRemovedException, IllegalStateTransitionException;

    public Object getDeviceIdentityMessage() {
        return identityMessage;
    }

    public abstract String getDeviceCategory();

    public abstract String getDeviceConfigReport();

    public int getState() {
        return sts.getState();
    }

    public String getReasonForState() {
        return sts.getReason();
    }
    /*
    public void beginCritical(Object critical) {
        criticality.beginCritical(critical);
    }

    public void endCritical(Object critical) {
        criticality.endCritical(critical);
    }

    public boolean runIfNonCritical(Runnable r) {
        return criticality.runIfNonCritical(r);
    }

    public boolean isCritical() {
        return criticality.isCritical();
    }

    public void waitOnCriticals() {
        criticality.waitOnCriticals();
    }
    */
}
