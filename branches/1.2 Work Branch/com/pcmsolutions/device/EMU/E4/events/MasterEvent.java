/*
 * DeviceEvent.java
 *
 * Created on January 27, 2003, 10:35 PM
 */

package com.pcmsolutions.device.EMU.E4.events;


import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.master.MasterListener;

/**
 *
 * @author  pmeehan
 */
public class MasterEvent extends java.util.EventObject {
    private DeviceContext device;

    public MasterEvent(Object source, DeviceContext device) {
        super(source);
        this.device = device;
    }

    public DeviceContext getDevice() {
        return device;
    }

    public String toString() {
        return "MasterEvent";
    }

    public void fire(MasterListener cl) {
    }
}
