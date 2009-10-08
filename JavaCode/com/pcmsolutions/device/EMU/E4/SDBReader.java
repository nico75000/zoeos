package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;

import java.util.List;
import java.util.Map;
import java.util.Set;

interface SDBReader {

    public int getDBCount();

    public List findEmptySamples(SampleContext sc, int reqd) throws NoSuchContextException;

    public List findEmptySamples(SampleContext sc, int reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException;

    public boolean seesSample(SampleContext sc, Integer sample);

    public boolean readsSample(SampleContext sc, Integer sample);

    public boolean hasSample(SampleContext sc, Integer sample);

    public boolean isSampleInitialized(Integer sample) throws NoSuchSampleException;

    public SampleContext getRootContext();

    public String getSampleName(Integer sample) throws NoSuchSampleException;

    public String getSampleNameExtended(SampleContext pc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void setSampleName(SampleContext sc, Integer sample, String name) throws SampleEmptyException, NoSuchSampleException, NoSuchContextException;

    public void refreshSample(SampleContext sc, Integer sample) throws NoSuchContextException, NoSuchSampleException;

    public Set getSampleIndexesInContext(SampleContext sc) throws NoSuchContextException;

    public Set getReadableSampleIndexes(SampleContext sc) throws NoSuchContextException;

    public Map getSampleNamesInContext(SampleContext sc) throws NoSuchContextException;

    public Map getUserSampleNamesInContext(SampleContext sc) throws NoSuchContextException;

    public void changeSampleObject(SampleContext sc, Integer sample, Object pobj) throws NoSuchSampleException, NoSuchContextException;

    public void lockSampleRead(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void assertSampleNamed(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public boolean tryLockSampleRead(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public boolean tryLockSampleWrite(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void lockSampleWrite(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public SampleObject[] getSampleRW(SampleContext sc, Integer readSample, Integer writeSample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException;

    // Object[0] guaranteed to be a SampleObject
    // Object[1] could be either a SampleObject or EmptySample
    public Object[] getSampleRC(SampleContext sc, Integer readSample, Integer writeSample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException;

    public void unlockSample(Integer sample);

    public SampleObject getSampleRead(SampleContext sc, Integer sample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException;

    public SampleObject getSampleWrite(SampleContext sc, Integer sample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException;

    public String tryGetSampleSummary(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    // can return either a SampleObject or EmptySample
    public Object getSampleObjectWrite(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public int getSampleState(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public double getInitializationStatus(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public boolean isSampleWriteLocked(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void release();

}

