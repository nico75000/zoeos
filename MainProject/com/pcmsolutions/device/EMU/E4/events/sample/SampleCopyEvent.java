package com.pcmsolutions.device.EMU.E4.events.sample;

import com.pcmsolutions.device.EMU.E4.sample.SampleListener;

/**
 * User: paulmeehan
 * Date: 30-Aug-2004
 * Time: 12:39:55
 */
public class SampleCopyEvent extends SampleRefreshEvent {
    Integer srcIndex;
    String destName;

    public SampleCopyEvent(Object source, Integer sample, Integer destSample, String destName) {
        super(source, destSample);
        this.srcIndex = sample;
        this.destName = destName;
    }

    public void fire(SampleListener sl) {
        if (sl != null)
            sl.sampleRefreshed(new SampleRefreshEvent(this, getIndex()));
    }

    public Integer getSrcIndex() {
        return srcIndex;
    }

    public String getDestName() {
        return destName;
    }
}
