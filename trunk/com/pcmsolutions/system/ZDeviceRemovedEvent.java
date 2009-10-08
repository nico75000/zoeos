package com.pcmsolutions.system;



/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Mar-2003
 * Time: 19:33:07
 * To change this template use Options | File Templates.
 */
public class ZDeviceRemovedEvent extends ZDeviceEvent {
    public ZDeviceRemovedEvent(Object source, ZExternalDevice device) {
        super(source, device);
    }

    public void fire(ZDeviceListener zdl) {
        if (zdl != null)
            zdl.deviceRemoved(this);
    }
}
