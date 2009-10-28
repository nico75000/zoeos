package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;


public class PresetCopyEvent extends PresetEvent {
    private Integer sourcePreset;

    public PresetCopyEvent(Object source, Integer srcPreset, Integer destPreset) {
        super(source, destPreset);
        this.sourcePreset = srcPreset;
    }

    public Integer getSourcePreset() {
        return sourcePreset;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetRefreshed(new PresetInitializeEvent(this, getIndex()));
    }
}

