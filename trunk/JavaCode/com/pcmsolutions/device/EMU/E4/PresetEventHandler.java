package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.PresetEvent;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


interface PresetEventHandler{
    public void postPresetEvent(PresetEvent ev);

    public void addPresetListener(PresetListener pl, Integer preset);

    public void removePresetListener(PresetListener pl, Integer preset);
}

