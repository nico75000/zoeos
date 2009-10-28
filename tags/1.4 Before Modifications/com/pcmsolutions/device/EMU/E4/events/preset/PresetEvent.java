package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentEvent;


public abstract class PresetEvent extends ContentEvent<PresetListener> {
    public PresetEvent(Object source, Integer preset) {
        super(source, preset);
    }

    public boolean independentOf(ContentEvent ev) {
        return !(ev instanceof PresetEvent ) || !getIndex().equals(ev.getIndex());
    }

    public boolean subsumes(ContentEvent ev) {
        return false;
    }
}
