/*
 * PresetDBEvent.java
 *
 * Created on January 27, 2003, 10:41 PM
 */

package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.DeviceListener;

/**
 *
 * @author  pmeehan
 */
public class PresetDBEvent extends DeviceEvent {
    private String presetDBKey;

    public PresetDBEvent(Object source, DeviceContext device, String presetDBKey) {
        super(source, device);
        this.presetDBKey = presetDBKey;
    }

    public String toString() {
        return "PresetDBEvent";
    }

    public void fire(DeviceListener dl) {
    }

    public Object getPresetDBKey() {
        return presetDBKey;
    }
}
