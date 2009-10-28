package com.pcmsolutions.system;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Mar-2003
 * Time: 19:33:07
 * To change this template use Options | File Templates.
 */
public abstract class ZDeviceEvent extends ZEvent {
    private ZExternalDevice device;

    public ZDeviceEvent(Object source, ZExternalDevice device) {
        super(source);
        this.device = device;
    }

    public ZExternalDevice getDevice() {
        return device;
    }

    public abstract void fire(ZDeviceListener zdl);
}
