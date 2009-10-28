package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.events.preset.GroupEvent;

import java.util.ArrayList;

public class GroupChangeEvent extends GroupEvent {
    private Integer[] parameters;
    private Integer[] values;
    private VoiceChangeEvent[] events;

    public GroupChangeEvent(Object source, Integer preset, Integer group, Integer[] voices, Integer[] parameters, Integer[] values) {
        super(source, preset, group, voices);
        this.parameters = (Integer[]) parameters.clone();
        this.values = (Integer[]) values.clone();
        createVoiceEvents();
    }

    void createVoiceEvents() {
        Integer[] voices = getVoices();
        events = new VoiceChangeEvent[voices.length];
        for (int i = 0; i < voices.length; i++)
            events[i] = new VoiceChangeEvent(this, getIndex(), voices[i], getParameters(), getValues());
    }

    public Integer[] getParameters() {
        return (Integer[]) parameters.clone();
    }

    public boolean containsId(Integer id) {
        for (int i = 0; i < parameters.length; i++)
            if (parameters[i].equals(id))
                return true;
        return false;
    }

    public Integer[] getValues() {
        return (Integer[]) values.clone();
    }

    public void fire(PresetListener pl) {
        Integer[] voices = getVoices();
        if (pl != null) {
            for (int i = 0; i < voices.length; i++)
                try {
                    pl.voiceChanged(events[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
}

