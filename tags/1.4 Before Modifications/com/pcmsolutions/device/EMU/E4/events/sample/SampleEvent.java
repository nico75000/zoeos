package com.pcmsolutions.device.EMU.E4.events.sample;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentEvent;


public abstract class SampleEvent extends ContentEvent<SampleListener> {

    public SampleEvent(Object source, Integer sample) {
        super(source, sample);
    }

    public abstract void fire(SampleListener sl);

    public boolean independentOf(ContentEvent ev) {
        return !(ev instanceof SampleEvent) || !getIndex().equals(ev.getIndex());
    }

    public boolean subsumes(ContentEvent ev) {
        return false;
    }
}
