package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;


public class SampleEvent extends java.util.EventObject {

    private Integer sample;

    public SampleEvent(Object source, Integer sample) {
        super(source);
        this.sample = sample;
    }

    public String toString() {
        return "SampleEvent";
    }

    public Integer getSample() {
        return sample;
    }

    public void fire(SampleListener pl) {
        throw new IllegalArgumentException("Cannot fire a basic SampleEvent.");
    }
}
