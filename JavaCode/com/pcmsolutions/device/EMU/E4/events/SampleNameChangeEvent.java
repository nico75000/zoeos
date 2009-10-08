/*
 * PresetNameChangeEvent.java
 *
 * Created on February 7, 2003, 4:43 AM
 */

package com.pcmsolutions.device.EMU.E4.events;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;


/**
 *
 * @author  pmeehan
 */
public class SampleNameChangeEvent extends SampleEvent {
    public SampleNameChangeEvent(Object source, Integer sample) {
        super(source, sample);
    }

    public String toString() {
        return "SampleNameChangeEvent";
    }

    public void fire(SampleListener sl) {
        if (sl != null)
            sl.sampleNameChanged(this);
    }
}
