package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentEvent;

import java.util.Arrays;

public class LinkChangeEvent extends LinkEvent implements ParameterEvent {
    private Integer[] ids;
    private Integer[] values;

    public LinkChangeEvent(Object source, Integer preset, Integer link, Integer[] parameters, Integer[] values) {
        super(source, preset, link);
        this.ids = (Integer[]) parameters.clone();
        this.values = (Integer[]) values.clone();
    }

    public Integer[] getValues() {
        return values;
    }

    public Integer[] getIds() {
        return ids;
    }

    public boolean containsId(Integer id) {
        for (int i = 0; i < ids.length; i++)
            if (ids[i].equals(id))
                return true;
        return false;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.linkChanged(this);
    }

    public boolean equals(Object o) {
        if (o instanceof LinkChangeEvent) {
            LinkChangeEvent lce = (LinkChangeEvent) o;
            return getIndex().equals(lce.getIndex()) && getLink().equals(lce.getLink());
        }
        return false;
    }

    public boolean subsumes(ContentEvent ev) {
        if (equals(ev))
            if (Arrays.asList(ids).containsAll(Arrays.asList((Object[]) ((LinkChangeEvent) ev).getIds())))
                return true;
        return false;
    }

    public boolean independentOf(ContentEvent ev) {
        if (super.independentOf(ev) || (ev instanceof ParameterEvent && !equals(ev)))
            return true;
        return false;
    }
}

