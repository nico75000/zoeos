/*
 * DeviceException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.device.EMU.database.ContextException;

/**
 *
 * @author  pmeehan
 */
public class SampleException extends ContextException {
    protected Integer sample = IntPool.get(Integer.MIN_VALUE);

    public SampleException(Integer sample) {
        super("");
        this.sample = sample;
    }

    public SampleException(Integer sample, String msg) {
        super(msg);
        this.sample = sample;
    }

    public Integer getSample() {
        return sample;
    }
}
