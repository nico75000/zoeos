package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;


public class SampleRefreshEvent extends SampleEvent {

    public SampleRefreshEvent(Object source, Integer sample) {
        super(source, sample);
    }

    public String toString() {
        return "sampleRefreshEvent";
    }

    public void fire(SampleListener sl) {
        if (sl != null)
            sl.sampleRefreshed(this);
    }
}

