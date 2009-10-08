package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class ZoneAddEvent extends ZoneEvent {
    private int num;

    public ZoneAddEvent(Object source, Integer preset, Integer voice, Integer zone, int num) {
        super(source, preset, voice, zone);
        this.num = num;
    }

    public String toString() {
        return "ZoneAddEvent";
    }

    public int getNumberOfZones() {
        return num;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.zoneAdded(this);
    }
}

