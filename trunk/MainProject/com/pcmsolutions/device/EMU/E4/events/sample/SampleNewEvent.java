package com.pcmsolutions.device.EMU.E4.events.sample;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.callback.Callback;

/**
 * User: paulmeehan
 * Date: 26-Aug-2004
 * Time: 04:59:52
 */
public abstract class SampleNewEvent extends SampleInitializeEvent implements Callback{
    IsolatedSample isolatedSample;
    SampleDescriptor sampleDescriptor;
    String name;
    ProgressCallback progressCallback;
    boolean handled = false;

    public SampleNewEvent(Object source, Integer sample, IsolatedSample dropSample, String name, ProgressCallback progressCallback) {
        super(source, sample);
        isolatedSample = dropSample;
        this.name = name;
        this.progressCallback = progressCallback;
    }

    public IsolatedSample getIsolatedSample() {
        return isolatedSample;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setFinished(boolean handled) {
        this.handled = handled;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public SampleDescriptor getSampleDescriptor() {
        return sampleDescriptor;
    }

    public void setSampleDescriptor(SampleDescriptor sampleDescriptor) {
        this.sampleDescriptor = sampleDescriptor;
    }

    public String getName() {
        return name;
    }
}
