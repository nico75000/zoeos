package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class ZoneRemoveEvent extends ZoneEvent {

    public ZoneRemoveEvent(Object source, Integer preset, Integer voice, Integer zone) {
        super(source, preset, voice, zone);
    }

    public String toString() {
        return "ZoneRemoveEvent";
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.zoneRemoved(this);
    }

}

