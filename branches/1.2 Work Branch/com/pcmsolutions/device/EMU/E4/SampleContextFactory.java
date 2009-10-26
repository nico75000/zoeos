package com.pcmsolutions.device.EMU.E4;


import com.pcmsolutions.device.EMU.E4.sample.SampleContext;

interface SampleContextFactory {

    public SampleContext newSampleContext(String name, SampleDatabaseProxy pdbp);

    public Object initializeSampleAtIndex(Integer index, SampleEventHandler peh);

    public Object initializeSampleAtIndex(Integer index, String name, SampleEventHandler peh);

    public double getSampleInitializationStatus(Integer index);

    public String initializeSampleNameAtIndex(Integer index, SampleEventHandler peh);
}

