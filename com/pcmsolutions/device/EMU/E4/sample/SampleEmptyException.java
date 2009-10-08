/*
 * NoSuchPresetException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.DeviceContext;

/**
 *
 * @author  pmeehan
 */
public class SampleEmptyException extends SampleException {

    public SampleEmptyException(Integer sample) {
        super(sample, DeviceContext.EMPTY_SAMPLE);
    }

    public SampleEmptyException(Integer sample, String name) {
        super(sample, name);
    }

    public SampleEmptyException(Integer sample, String name, String msg) {
        super(sample, name, msg);
    }
}
