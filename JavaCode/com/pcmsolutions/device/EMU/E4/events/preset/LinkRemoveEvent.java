package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.LinkEvent;

public class LinkRemoveEvent extends LinkEvent {

    public LinkRemoveEvent(Object source, Integer preset, Integer link) {
        super(source, preset, link);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.linkRemoved(this);
    }
}

