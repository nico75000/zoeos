package com.pcmsolutions.device.EMU.E4.events.sample.requests;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleEvent;

/**
 * User: paulmeehan
 * Date: 26-Aug-2004
 * Time: 07:31:26
 */
public class SampleIsolationRequest extends SampleRequestEvent<IsolatedSample> {
    SampleDownloadDescriptor sampleDownloadDescriptor;
    String name;

    public SampleIsolationRequest(Object source, String name, SampleDownloadDescriptor sdd) {
        super(source, sdd.getIndex());
        this.sampleDownloadDescriptor = sdd;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SampleDownloadDescriptor getSampleDownloadDescriptor() {
        return sampleDownloadDescriptor;
    }
}
