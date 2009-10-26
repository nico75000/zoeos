package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class VoiceRemoveEvent extends VoiceEvent {

    public VoiceRemoveEvent(Object source, Integer preset, Integer voice) {
        super(source, preset, voice);
    }

    public String toString() {
        return "VoiceRemoveEvent";
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.voiceRemoved(this);
    }

}

