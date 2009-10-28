package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.ZoneEvent;

public class ZoneRemoveEvent extends ZoneEvent {

    public ZoneRemoveEvent(Object source, Integer preset, Integer voice, Integer zone) {
        super(source, preset, voice, zone);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.zoneRemoved(this);
    }
}

