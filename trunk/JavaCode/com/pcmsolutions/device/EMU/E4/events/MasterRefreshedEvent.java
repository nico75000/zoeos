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
public class MasterRefreshedEvent extends MasterEvent {

    public MasterRefreshedEvent(Object source, DeviceContext device) {
        super(source, device);
    }

    public String toString() {
        return "MasterRefreshedEvent";
    }

    public void fire(MasterListener ml) {
        if (ml != null)
            ml.masterRefreshed(this);
    }
}
