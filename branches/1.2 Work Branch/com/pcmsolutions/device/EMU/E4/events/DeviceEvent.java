/*
 * DeviceEvent.java
 *
 * Created on January 27, 2003, 10:35 PM
 */

package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.DeviceListener;

/**
 *
 * @author  pmeehan
 */
public class DeviceEvent extends java.util.EventObject {
    private DeviceContext device;

    public DeviceEvent(Object source, DeviceContext device) {
        super(source);
    }

    public String toString() {
        return "PresetDBEvent";
    }

    public void fire(DeviceListener dl) {
    }

    public DeviceContext getDevice() {
        return device;
    }
}
