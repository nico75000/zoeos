package com.pcmsolutions.device.EMU.E4.events.sample;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;


public class SampleChangeEvent extends SampleEvent {

    private Integer[] parameters;

    public SampleChangeEvent(Object source, Integer sample, Integer[] parameters) {
        super(source, sample);
        this.parameters = (Integer[]) parameters.clone();
    }

    public Integer[] getParameters() {
        return parameters;
    }

    public void fire(SampleListener sl) {
        if (sl != null)
            sl.sampleChanged(this);
    }
}
