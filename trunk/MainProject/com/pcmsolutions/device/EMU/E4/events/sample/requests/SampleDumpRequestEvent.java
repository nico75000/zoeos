package com.pcmsolutions.device.EMU.E4.events.sample.requests;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleEvent;

/**
 * User: paulmeehan
 * Date: 26-Mar-2004
 * Time: 19:09:27
 */
public class SampleDumpRequestEvent extends SampleRequestEvent<SampleDumpResult> {    
    public SampleDumpRequestEvent(Object source, Integer sample) {
        super(source, sample);
    }
}
