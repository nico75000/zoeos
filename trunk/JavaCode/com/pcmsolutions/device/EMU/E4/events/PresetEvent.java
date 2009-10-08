package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


public class PresetEvent extends java.util.EventObject {

    private Integer preset;

    public PresetEvent(Object source, Integer preset) {
        super(source);
        this.preset = preset;
    }

    public String toString() {
        return "PresetEvent";
    }

    public Integer getPreset() {
        return preset;
    }

    public void fire(PresetListener pl) {
        throw new IllegalArgumentException("Cannot fire a basic PresetEvent.");
    }
}
