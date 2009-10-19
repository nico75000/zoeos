package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class VoiceChangeEvent extends VoiceEvent {

    private Integer preset;

    private Integer voice;

    private Integer[] parameters;

    public VoiceChangeEvent(Object source, Integer preset, Integer voice, Integer[] parameters) {
        super(source, preset, voice);
        this.parameters = (Integer[]) parameters.clone();
    }

    public String toString() {
        return "VoiceChangeEvent";
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
            pl.voiceChanged(this);
    }
}

