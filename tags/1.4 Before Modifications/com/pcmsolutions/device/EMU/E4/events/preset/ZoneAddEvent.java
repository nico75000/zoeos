package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class ZoneAddEvent extends ZoneEvent {
    public ZoneAddEvent(Object source, Integer preset, Integer voice, Integer zone) {
        super(source, preset, voice, zone);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.zoneAdded(this);
    }
}

