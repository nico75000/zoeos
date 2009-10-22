package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.device.EMU.E4.events.preset.VoiceAddEvent;


public class VoiceCopyEvent extends VoiceAddEvent {
    private Integer sourcePreset;
    private Integer sourceVoice;
    private Integer group;

    public VoiceCopyEvent(Object source, Integer preset, Integer voice, Integer sourcePreset, Integer sourceVoice, Integer group) {
        super(source, preset, voice);
        this.sourcePreset = sourcePreset;
        this.sourceVoice = sourceVoice;
        this.group = group;
    }

    public Integer getSourcePreset() {
        return sourcePreset;
    }

    public Integer getGroup() {
        return group;
    }

    public Integer getSourceVoice() {
        return sourceVoice;
    }
}

