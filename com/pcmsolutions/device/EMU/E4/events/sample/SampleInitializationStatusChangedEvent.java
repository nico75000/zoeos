/*
 * PresetInitializeEvent.java
 *
 * Created on February 14, 2003, 10:15 PM
 */

package com.pcmsolutions.device.EMU.E4.events.sample;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleEvent;


/**
 *
 * @author  pmeehan
 */
public class SampleInitializationStatusChangedEvent extends SampleEvent {
    public SampleInitializationStatusChangedEvent(Object source, Integer sample) {
        super(source, sample);
    }

    public void fire(SampleListener sl) {
        if (sl != null)
            sl.sampleInitializationStatusChanged(this);
    }
}
