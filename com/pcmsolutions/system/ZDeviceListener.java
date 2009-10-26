package com.pcmsolutions.system;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Mar-2003
 * Time: 19:32:31
 * To change this template use Options | File Templates.
 */
public interface ZDeviceListener {
    public void deviceStarted(ZDeviceStartedEvent ev);

    public void deviceStopped(ZDeviceStoppedEvent ev);

    public void devicePending(ZDevicePendingEvent ev);

    public void deviceRemoved(ZDeviceRemovedEvent ev);
}
