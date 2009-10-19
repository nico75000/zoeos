package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.ContextReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextReadableSampleZCommandMarker;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


class Impl_ContextReadableSample extends Impl_ReadableSample implements ContextReadableSample {
    private static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextReadableSampleZCommandMarker.class, "");

    static {
        SampleClassManager.addSampleClass(Impl_ContextReadableSample.class, null);
    }

    public Impl_ContextReadableSample(SampleContext sc, Integer sample) {
        super(sc, sample);
    }

    public SampleContext getSampleContext() {
        return sc;
    }

    public Set getSampleIndexesInContext() {
        try {
            return sc.getSampleIndexesInContext();
        } catch (NoSuchContextException e) {
            return new HashMap().keySet();
        }
    }

    public Map getSampleNamesInContext() {
        try {
            return sc.getSampleNamesInContext();
        } catch (NoSuchContextException e) {
            return new HashMap();
        }
    }

    public Map getUserSampleNamesInContext() {
        try {
            return sc.getUserSampleNamesInContext();
        } catch (NoSuchContextException e) {
            return new HashMap();
        }
    }

    public List findEmptySamples(int reqd) throws NoSuchContextException {
        return sc.findEmptySamplesInContext(reqd);
    }

    public boolean sampleEmpty(Integer sample) throws NoSuchSampleException {
        try {
            return sc.isSampleEmpty(sample);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }


    public ZCommand[] getZCommands() {
        return ZUtilities.concatZCommands(super.getZCommands(), this.cmdProviderHelper.getCommandObjects(this));
    }
}

