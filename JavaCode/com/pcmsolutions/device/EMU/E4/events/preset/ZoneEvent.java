package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.events.preset.VoiceEvent;


public abstract class ZoneEvent extends VoiceEvent {
    private Integer zone;

    public ZoneEvent(Object source, Integer preset, Integer voice, Integer zone) {
        super(source, preset, voice);
        this.zone = zone;
    }

    public Integer getZone() {
        return zone;
    }
}

