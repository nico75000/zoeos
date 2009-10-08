package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.SampleEvent;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;


interface SampleEventHandler {
    public void postSampleEvent(SampleEvent ev);

    public void addSampleListener(SampleListener pl, Integer sample);

    public void removeSampleListener(SampleListener pl, Integer sample);
}

