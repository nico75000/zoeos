package com.pcmsolutions.device.EMU.E4.events.preset.requests;

import java.util.List;


public class PresetParametersRequestEvent extends PresetRequestEvent<List<Integer>> {
    private Integer[] parameters;

    public PresetParametersRequestEvent(Object source, Integer preset, Integer[] parameters) {
        super(source, preset);
        this.parameters = (Integer[]) parameters.clone();
    }

    public Integer[] getParameters() {
        return (Integer[]) parameters.clone();
    }

    public boolean containsId(Integer id) {
        for (int i = 0; i < parameters.length; i++)
            if (parameters[i].equals(id))
                return true;
        return false;
    }
}

