package com.pcmsolutions.device.EMU.E4.events.sample.requests;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleEvent;

/**
 * User: paulmeehan
 * Date: 26-Mar-2004
 * Time: 19:09:27
 */
public class SampleNameRequestEvent extends SampleEvent {
    private String name = null;

    public SampleNameRequestEvent(Object source, Integer sample) {
        super(source, sample);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public void fire(SampleListener pl) {
    }
}
