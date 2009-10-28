package com.pcmsolutions.device.EMU.E4.events.preset;


public abstract class GroupEvent extends PresetEvent {
    private Integer group;
    private Integer[] voices;

    public GroupEvent(Object source, Integer preset, Integer group, Integer[] voices) {
        super(source, preset);
        this.group = group;
        this.voices = voices;
    }

    public Integer[] getVoices() {
        return (Integer[])voices.clone();
    }

    public Integer getGroup() {
        return group;
    }
}

