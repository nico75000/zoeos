package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentEvent;

import java.util.Arrays;

public class ZoneChangeEvent extends ZoneEvent implements ParameterEvent {

    private Integer[] ids;
    private Integer[] values;

    public ZoneChangeEvent(Object source, Integer preset, Integer voice, Integer zone, Integer[] parameters, Integer[] values) {
        super(source, preset, voice, zone);
        this.ids = (Integer[]) parameters.clone();
        this.values = (Integer[]) values.clone();
    }

    public Integer[] getIds() {
        return (Integer[]) ids.clone();
    }

    public Integer[] getValues() {
        return (Integer[]) values.clone();
    }

    public boolean containsId(Integer id) {
        for (int i = 0; i < ids.length; i++)
            if (ids[i].equals(id))
                return true;
        return false;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.zoneChanged(this);
    }


    public boolean equals(Object o) {
        if (o instanceof ZoneChangeEvent) {
            ZoneChangeEvent zce = (ZoneChangeEvent) o;
            return getIndex().equals(zce.getIndex()) && getVoice().equals(zce.getVoice()) && getZone().equals(zce.getZone());
        }
        return false;
    }

    public boolean subsumes(ContentEvent ev) {
        if (equals(ev))
            if (Arrays.asList(ids).containsAll(Arrays.asList((Object[]) ((ZoneChangeEvent) ev).getIds())))
                return true;
        return false;
    }

    public boolean independentOf(ContentEvent ev) {
        if (super.independentOf(ev) || (ev instanceof ParameterEvent && !equals(ev)))
            return true;
        return false;
    }
}

