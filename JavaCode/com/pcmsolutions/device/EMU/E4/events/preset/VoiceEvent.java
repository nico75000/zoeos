package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.events.preset.PresetEvent;


public abstract class VoiceEvent extends PresetEvent {
    private Integer voice;

    public VoiceEvent(Object source, Integer preset, Integer voice) {
        super(source, preset);
        this.voice = voice;
    }

    public Integer getVoice() {
        return voice;
    }
}

