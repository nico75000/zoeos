package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

public class GroupCombineEvent extends PresetEvent {
    private Integer group;

    public GroupCombineEvent(Object source, Integer preset, Integer group) {
        super(source, preset);
        this.group = group;
    }

    public Integer getGroup() {
        return group;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetRefreshed(new PresetInitializeEvent(this, getIndex()));
    }
}

