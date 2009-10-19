package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;

import java.util.List;

interface SDBWriter extends SDBReader {

    public void releaseContext(SampleContext sc) throws NoSuchContextException;

    public void releaseContextSample(SampleContext sc, Integer sample) throws NoSuchContextException, NoSuchSampleException;

    public SampleContext newContext(SampleContext sc, String name, Integer[] samples) throws NoSuchSampleException, NoSuchContextException;

    public void removeSamplesFromContext(SampleContext src, Integer[] samples) throws NoSuchContextException, NoSuchSampleException;

    public void addSamplesToContext(SampleContext dest, Integer[] samples) throws NoSuchContextException, NoSuchSampleException;

    public List expandContextWithEmptySamples(SampleContext src, SampleContext dest, Integer reqd) throws NoSuchContextException;

    public void release();
}

