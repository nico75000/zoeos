/*
 * SampleObject.java
 *
 * Created on February 9, 2003, 2:11 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;

import java.util.Map;
import java.util.Set;


/**
 *
 * @author  pmeehan
 */
public interface ContextReadableSample extends ReadableSample {

    public Set getSampleIndexesInContext();

    public Map getSampleNamesInContext();

    public Map getUserSampleNamesInContext();

    public java.util.List findEmptySamples(int reqd) throws NoSuchContextException;

    public boolean sampleEmpty(Integer sample) throws NoSuchSampleException;

    public SampleContext getSampleContext();
}
