/*
 * SampleObject.java
 *
 * Created on February 9, 2003, 2:11 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextReadableSampleZCommandMarker;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.ZCommandProviderHelper;

import java.util.Map;
import java.util.Set;


/**
 *
 * @author  pmeehan
 */
public interface ContextReadableSample extends ReadableSample {
    final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextReadableSampleZCommandMarker.class, ReadableSample.cmdProviderHelper);

    public Map<Integer, String> getUserSampleNamesInContext() throws DeviceException;

    public SampleContext getSampleContext();
}
