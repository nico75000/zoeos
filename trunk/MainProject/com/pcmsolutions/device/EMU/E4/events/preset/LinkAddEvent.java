package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.LinkEvent;

public class LinkAddEvent extends LinkEvent {

    public LinkAddEvent(Object source, Integer preset, Integer link) {
        super(source, preset, link);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.linkAdded(this);
    }
}

