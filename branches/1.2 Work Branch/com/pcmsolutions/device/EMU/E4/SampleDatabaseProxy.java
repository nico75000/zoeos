package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;

import java.util.Set;

interface SampleDatabaseProxy {

    public SampleEventHandler getSampleEventHandler();

    public void addSampleListener(SampleListener pl, Integer[] samples);

    public void removeSampleListener(SampleListener pl, Integer[] samples);

    public SDBWriter getDBWrite();

    public SDBReader getDBRead();

    public Set getReadableSamples();

    public SampleContext getRootContext();

    public PresetContext getRootPresetContext();
}

