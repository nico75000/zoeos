package com.pcmsolutions.device.EMU.E4.events.preset.requests;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.VoiceEvent;

import java.util.List;

public class VoiceParametersRequestEvent extends PresetRequestEvent<List<Integer>> {
    private Integer[] parameters;
    private Integer voice;

    public VoiceParametersRequestEvent(Object source, Integer preset, Integer voice, Integer[] parameters) {
        super(source, preset);
        this.parameters = (Integer[]) parameters.clone();
        this.voice = voice;
    }

    public Integer[] getParameters() {
        return (Integer[])parameters.clone();
    }

    public Integer getVoice() {
        return voice;
    }

    public boolean containsId(Integer id) {
        for (int i = 0; i < parameters.length; i++)
            if (parameters[i].equals(id))
                return true;
        return false;
    }
}

