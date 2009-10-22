package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.sample.ContextReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.system.ZCommand;

import java.util.Map;


class Impl_ContextReadableSample extends Impl_ReadableSample implements ContextReadableSample {
    static {
        SampleClassManager.addSampleClass(Impl_ContextReadableSample.class, null);
    }

    public Impl_ContextReadableSample(SampleContext sc, Integer sample) {
        super(sc, sample);
    }

    public SampleContext getSampleContext() {
        return sc;
    }

    public Map<Integer, String> getUserSampleNamesInContext() throws DeviceException {
        return sc.getContextUserNamesMap();
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return ContextReadableSample.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ContextReadableSample.cmdProviderHelper.getSupportedMarkers();
    }
}

