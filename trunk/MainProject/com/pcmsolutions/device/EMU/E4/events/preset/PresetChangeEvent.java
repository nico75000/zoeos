package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentEvent;

import java.util.Arrays;

public class PresetChangeEvent extends PresetEvent implements ParameterEvent {
    private Integer[] ids;
    private Integer[] values;

    public PresetChangeEvent(Object source, Integer preset, Integer[] parameters, Integer[] values) {
        super(source, preset);
        this.ids = (Integer[]) parameters.clone();
        this.values = (Integer[]) values.clone();
    }

    public Integer[] getIds() {
        return ids;
    }

    public Integer[] getValues() {
        return values;
    }

    public boolean containsId(Integer id) {
        for (int i = 0; i < ids.length; i++)
            if (ids[i].equals(id))
                return true;
        return false;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.presetChanged(this);
    }

    public boolean equals(Object o) {
        if (o instanceof PresetChangeEvent) {
            PresetChangeEvent zce = (PresetChangeEvent) o;
            return getIndex().equals(zce.getIndex());
        }
        return false;
    }

    public boolean subsumes(ContentEvent ev) {
        if (equals(ev))
            if (Arrays.asList(ids).containsAll(Arrays.asList((Object[]) ((PresetChangeEvent) ev).getIds())))
                return true;
        return false;
    }

    public boolean independentOf(ContentEvent ev) {
        if (super.independentOf(ev) || (ev instanceof ParameterEvent && !equals(ev)))
            return true;
        return false;
    }
}
