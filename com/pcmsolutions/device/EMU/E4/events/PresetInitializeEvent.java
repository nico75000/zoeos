/*
 * PresetInitializeEvent.java
 *
 * Created on February 14, 2003, 10:15 PM
 */

package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


/**
 *
 * @author  pmeehan
 */
public class PresetInitializeEvent extends PresetEvent {
    public PresetInitializeEvent(Object source, Integer preset) {
        super(source, preset);
    }

    public String toString() {
        return "PresetInitializeEvent";
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetInitialized(this);
    }
}
