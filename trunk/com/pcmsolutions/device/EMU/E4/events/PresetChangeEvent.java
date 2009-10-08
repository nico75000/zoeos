package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


public class PresetChangeEvent extends PresetEvent {

    private Integer[] parameters;

    public PresetChangeEvent(Object source, Integer preset, Integer[] parameters) {
        super(source, preset);
        this.parameters = (Integer[]) parameters.clone();
    }

    public String toString() {
        return "PresetChangeEvent";
    }

    public Integer[] getParameters() {
        return parameters;
    }

    public boolean containsId(Integer id) {
        for (int i = 0; i < parameters.length; i++)
            if (parameters[i].equals(id))
                return true;
        return false;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetChanged(this);
    }
}
