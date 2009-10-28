package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class VoiceAddEvent extends VoiceEvent {
    public VoiceAddEvent(Object source, Integer preset, Integer voice) {
        super(source, preset, voice);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.voiceAdded(this);
    }
}

