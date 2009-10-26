package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class LinkRemoveEvent extends LinkEvent {

    private Integer link;

    public LinkRemoveEvent(Object source, Integer preset, Integer link) {
        super(source, preset, link);
    }

    public String toString() {
        return "LinkRemoveEvent";
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.linkRemoved(this);
    }

}

