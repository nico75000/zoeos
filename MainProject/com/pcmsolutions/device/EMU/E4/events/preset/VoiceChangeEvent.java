package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentEvent;

import java.util.Arrays;

public class VoiceChangeEvent extends VoiceEvent implements ParameterEvent {
    private Integer[] ids;
    private Integer[] values;

    public VoiceChangeEvent(Object source, Integer preset, Integer voice, Integer[] parameters, Integer[] values) {
        super(source, preset, voice);
        this.ids = (Integer[]) parameters.clone();
        this.values = values;
    }

    public Integer[] getIds() {
        return ids;
    }

    public Integer[] getValues() {
        return (Integer[]) values.clone();
    }

    public boolean containsId(Integer id) {
        return indexOfId(id) != -1;
    }

    public int indexOfId(Integer id) {
        for (int i = 0; i < ids.length; i++)
            if (ids[i].equals(id))
                return i;
        return -1;
    }

    public void fire(PresetListener pl) {
        if (pl != null)
            pl.voiceChanged(this);
    }

    public boolean equals(Object o) {
        if (o instanceof VoiceChangeEvent) {
            VoiceChangeEvent vce = (VoiceChangeEvent) o;
            return getIndex().equals(vce.getIndex()) && getVoice().equals(vce.getVoice());
        }
        return false;
    }

    public boolean subsumes(ContentEvent ev) {
        if (equals(ev))
            if (Arrays.asList(ids).containsAll(Arrays.asList((Object[]) ((VoiceChangeEvent) ev).getIds())))
                return true;
        return false;
    }

    public boolean independentOf(ContentEvent ev) {
        if (super.independentOf(ev) || (ev instanceof ParameterEvent && !equals(ev)))
            return true;
        return false;
    }
}

