/*
 * PresetInitializeEvent.java
 *
 * Created on February 14, 2003, 10:15 PM
 */

package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;


/**
 *
 * @author  pmeehan
 */
public class SampleInitializeEvent extends SampleEvent {
    public SampleInitializeEvent(Object source, Integer sample) {
        super(source, sample);
    }

    public String toString() {
        return "SampleInitializeEvent";
    }

    public void fire(SampleListener sl) {
        if (sl != null)
            sl.sampleInitialized(this);
    }
}
