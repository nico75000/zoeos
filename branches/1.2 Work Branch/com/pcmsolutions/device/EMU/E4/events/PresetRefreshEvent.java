package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


public class PresetRefreshEvent extends PresetEvent {

    public PresetRefreshEvent(Object source, Integer preset) {
        super(source, preset);
    }

    public String toString() {
        return "PresetRefreshEvent";
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetRefreshed(this);
    }

}

