package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class VoiceAddEvent extends VoiceEvent {
    private int num;

    public VoiceAddEvent(Object source, Integer preset, Integer voice, int num) {
        super(source, preset, voice);
        this.num = num;
    }

    public String toString() {
        return "VoiceAddEvent";
    }

    public int getNumberOfVoices() {
        return num;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.voiceAdded(this);
    }

}

