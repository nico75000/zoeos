package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetEvent;


public class PresetInitializeEvent extends PresetEvent {

    public PresetInitializeEvent(Object source, Integer preset) {
        super(source, preset);
    }
    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetRefreshed(this);
    }
}

