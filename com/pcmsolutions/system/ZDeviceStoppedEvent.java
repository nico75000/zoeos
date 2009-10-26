package com.pcmsolutions.system;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Mar-2003
 * Time: 19:33:07
 * To change this template use Options | File Templates.
 */
public class ZDeviceStoppedEvent extends ZDeviceEvent {
    private String reason;

    public ZDeviceStoppedEvent(Object source, ZExternalDevice device, String reason) {
        super(source, device);
        if (reason == null || reason.equals(""))
            reason = "Not specified";
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void fire(ZDeviceListener zdl) {
        if (zdl != null)
            zdl.deviceStopped(this);
    }
}
