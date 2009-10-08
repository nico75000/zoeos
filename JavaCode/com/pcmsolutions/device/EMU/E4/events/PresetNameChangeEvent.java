/*
 * PresetNameChangeEvent.java
 *
 * Created on February 7, 2003, 4:43 AM
 */

package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


/**
 *
 * @author  pmeehan
 */
public class PresetNameChangeEvent extends PresetEvent {
    public PresetNameChangeEvent(Object source, Integer preset) {
        super(source, preset);
    }

    public String toString() {
        return "PresetNameChangeEvent";
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetNameChanged(this);
    }
}
