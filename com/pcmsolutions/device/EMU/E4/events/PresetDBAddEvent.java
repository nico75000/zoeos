/*
 * PresetDBAddedEvent.java
 *
 * Created on January 27, 2003, 10:32 PM
 */

package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.DeviceListener;

/**
 *
 * @author  pmeehan
 */
public class PresetDBAddEvent extends PresetDBEvent {

    public PresetDBAddEvent(Object source, DeviceContext device, String presetDBKey) {
        super(source, device, presetDBKey);
    }

    public String toString() {
        return "PresetDBAddEvent";
    }

    public void fire(DeviceListener dl) {
        if (dl != null)
            dl.presetDBAdded(this);
    }
}
