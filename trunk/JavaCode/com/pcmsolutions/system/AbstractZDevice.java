package com.pcmsolutions.system;

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

    protected StateMachineHelper sts = new StateMachineHelper(STATE_PENDING, stateTransitions, stateNames);

    protected Object identityMessage;

    // protected volatile JInternalFrame mainView = null;

    public SystemEntryPoint getSystemEntryPoint() {
        return new Impl_SystemEntryPoint(getClass(), getDeviceIdentityMessage().toString());
    }

    public AbstractZDevice(Object identityMessage) {
        this.identityMessage = identityMessage;
    }

    public abstract void startDevice() throws ZDeviceStartupException, IllegalStateTransitionException;

    public abstract void stopDevice(boolean waitForConfigurers, String reason) throws IllegalStateTransitionException;

    public abstract void removeDevice(boolean saveState) throws ZDeviceCannotBeRemovedException, IllegalStateTransitionException;

    public abstract void refreshDevice() throws ZDeviceRefreshException;

    public Object getDeviceIdentityMessage() {
        return identityMessage;
    }

    public abstract String getDeviceCategory();

    public abstract String getDeviceConfigReport();

    public int getState() {
        return sts.getState();
    }

    abstract public int getStateSynchronized();

    public abstract void markDuplicate() throws IllegalStateTransitionException;
}
