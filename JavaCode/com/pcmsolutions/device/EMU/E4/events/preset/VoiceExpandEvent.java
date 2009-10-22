package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.VoiceEvent;

public class VoiceExpandEvent extends VoiceEvent {

    public VoiceExpandEvent(Object source, Integer preset, Integer voice) {
        super(source, preset, voice);
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetRefreshed(new PresetInitializeEvent(this, getIndex()));
    }
}

