/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;


/**
 *
 * @author  pmeehan
 */

public interface ContextBasicEditableSample extends ContextReadableSample {
    public void setSampleName(String name) throws NoSuchSampleException, SampleEmptyException;

    public void lockSampleWrite() throws NoSuchSampleException, com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;

    public void eraseSample() throws NoSuchSampleException, SampleEmptyException;


}
