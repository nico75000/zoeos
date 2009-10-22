package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.VoiceEvent;

public class VoiceRemoveEvent extends VoiceEvent {

    public VoiceRemoveEvent(Object source, Integer preset, Integer voice) {
        super(source, preset, voice);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.voiceRemoved(this);
    }
}

