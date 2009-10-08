package com.pcmsolutions.device.EMU.E4.events;


public class ZoneEvent extends VoiceEvent {

    private Integer zone;

    public ZoneEvent(Object source, Integer preset, Integer voice, Integer zone) {
        super(source, preset, voice);
        this.zone = zone;
    }

    public String toString() {
        return "ZoneEvent";
    }

    public Integer getZone() {
        return zone;
    }

}

