package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class LinkAddEvent extends LinkEvent {
    private int num;

    public LinkAddEvent(Object source, Integer preset, Integer link, int num) {
        super(source, preset, link);
        this.num = num;
    }

    public String toString() {
        return "LinkAddEvent";
    }

    public int getNumberOfLinks() {
        return num;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.linkAdded(this);
    }

}

