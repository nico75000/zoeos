/*
 * NoSuchPresetException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.sample;


/**
 *
 * @author  pmeehan
 */
public class NoSuchSampleException extends SampleException {

    public NoSuchSampleException(Integer sample) {
        super(sample);
    }

    public NoSuchSampleException(Integer sample, String name) {
        super(sample, name);
    }

    public NoSuchSampleException(Integer sample, String name, String msg) {
        super(sample, name, msg);
    }
}
