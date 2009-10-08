package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class LinkChangeEvent extends LinkEvent {

    private Integer[] parameters;

    public LinkChangeEvent(Object source, Integer preset, Integer link, Integer[] parameters) {
        super(source, preset, link);
        this.parameters = (Integer[]) parameters.clone();
    }

    public String toString() {
        return "LinkChangeEvent";
    }

    public Integer[] getParameters() {
        return parameters;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.linkChanged(this);
    }

}

