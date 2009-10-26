package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class ZoneChangeEvent extends ZoneEvent {

    private Integer[] parameters;

    public ZoneChangeEvent(Object source, Integer preset, Integer voice, Integer zone, Integer[] parameters) {
        super(source, preset, voice, zone);
        this.parameters = (Integer[]) parameters.clone();
    }

    public String toString() {
        return "ZoneChangeEvent";
    }

    public Integer[] getParameters() {
        return parameters;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.zoneChanged(this);
    }

}

