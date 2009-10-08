/*
 * NoSuchPresetException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.system.IntPool;

/**
 *
 * @author  pmeehan
 */
public class SampleException extends Exception {
    protected Integer sample = IntPool.get(Integer.MIN_VALUE);
    protected String name = "=Unknown=";


    public SampleException(Integer sample) {
        this.sample = sample;
    }

    public SampleException(Integer sample, String name) {
        this.sample = sample;
        this.name = name;
    }

    public SampleException(Integer sample, String name, String msg) {
        super(msg);
        this.sample = sample;
        this.name = name;
    }

    public Integer getSample() {
        return sample;
    }


    public String getName() {
        return name;
    }

    public AggSampleName getAggName() {
        return new AggSampleName(sample, name);
    }

}
