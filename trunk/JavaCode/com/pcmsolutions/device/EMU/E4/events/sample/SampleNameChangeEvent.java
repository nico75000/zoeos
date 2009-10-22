/*
 * PresetNameChangeEvent.java
 *
 * Created on February 7, 2003, 4:43 AM
 */

package com.pcmsolutions.device.EMU.E4.events.sample;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleEvent;


/**
 *
 * @author  pmeehan
 */
public class SampleNameChangeEvent extends SampleEvent {
    String name;
    public SampleNameChangeEvent(Object source, Integer sample, String name) {
        super(source, sample);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void fire(SampleListener sl) {
        if (sl != null)
            sl.sampleNameChanged(this);
    }
}
