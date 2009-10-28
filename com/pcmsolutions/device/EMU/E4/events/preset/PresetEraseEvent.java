package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


public class PresetEraseEvent extends PresetEvent {

    public PresetEraseEvent(Object source, Integer preset) {
        super(source, preset);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetRefreshed(new PresetInitializeEvent(this, getIndex()));
    }
}

