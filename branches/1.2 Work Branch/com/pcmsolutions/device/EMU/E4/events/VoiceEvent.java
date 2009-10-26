package com.pcmsolutions.device.EMU.E4.events;


public class VoiceEvent extends PresetEvent {

    private Integer voice;

    public VoiceEvent(Object source, Integer preset, Integer voice) {
        super(source, preset);
        this.voice = voice;
    }

    public String toString() {
        return "VoiceEvent";
    }

    public Integer getVoice() {
        return voice;
    }

}

