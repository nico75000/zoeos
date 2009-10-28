/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextBasicEditableSampleZCommandMarker;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;


/**
 *
 * @author  pmeehan
 */

public interface ContextBasicEditableSample extends ContextReadableSample {
    final  ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextBasicEditableSampleZCommandMarker.class, ContextReadableSample.cmdProviderHelper);

    public void setSampleName(String name) throws SampleException;

    public void eraseSample() throws SampleException;

}
