package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.SampleInitializationStatusChangedEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:43:45
 * To change this template use Options | File Templates.
 */
class SampleInitializationMonitor {
    private SampleEventHandler peh;
    private Integer sample;

    public SampleInitializationMonitor(Integer sample, SampleEventHandler peh) {
        this.peh = peh;
        this.sample = sample;
    }

    public double getStatus() {
        return status;
    }

    public void setStatus(double status) {
        this.status = status;
        peh.postSampleEvent(new SampleInitializationStatusChangedEvent(this, sample));
    }

    private volatile double status = -1;

}
