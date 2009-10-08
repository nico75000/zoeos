package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.SampleNameChangeEvent;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.system.Nameable;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import java.io.Serializable;

class SampleObject implements Nameable, ZDisposable, Comparable, Serializable {
    private transient final int MAX_NAME_LENGTH = 16;
    private Integer sample;
    private String name;
    private transient SampleEventHandler seh;
    private SampleDescriptor sampleDescriptor;
    private String summary;

    // copy constructor same sample database
    public SampleObject(Integer sample, SampleObject src) {
        this(sample, src, src.seh);
    }

    // new constructor
    public SampleObject(Integer sample, String name, SampleEventHandler peh, SampleDescriptor sampleDescriptor) {
        this.sample = sample;
        this.name = name;
        this.seh = peh;
        setSampleDescriptor(sampleDescriptor);
    }

    // copy constructor with translation to given ParameterContext
    public SampleObject(Integer sample, SampleObject src, SampleEventHandler seh) {
        this.seh = seh;
        this.name = src.name;
        this.sample = sample;
        setSampleDescriptor(src.sampleDescriptor);
    }

    public String getSummary() {
        return summary;
    }

    public SampleDescriptor getSampleDescriptor() {
        return sampleDescriptor;
    }

    public void setSampleDescriptor(SampleDescriptor sampleDescriptor) {
        this.sampleDescriptor = sampleDescriptor;
        summary = ZUtilities.makeSampleSummary(sampleDescriptor);
        if (summary == null && sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE)
            summary = "ROM Sample";
        else if (summary == null && sample.intValue() < DeviceContext.BASE_ROM_SAMPLE)
            summary = "User Sample";
    }

//    public File getLocalFile() {
    //      return localCopy;
    //}

    public void setSampleNumber(Integer sample) {
        this.sample = sample;
    }

    public String toString() {
        return name;
    }

    public Integer getSample() {
        return sample;
    }

    public void setSample(Integer sample) {
        this.sample = sample;
    }

    public SampleEventHandler getSeh() {
        return seh;
    }

    public void setSeh(SampleEventHandler seh) {
        this.seh = seh;
    }

    public String getName() {
        return name;
    }

    // public Integer getOriginalIndex() {
    //   return sample;
    // }

    //  public boolean isROMSample() {
    //     return sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE;
    // }

    public void setName(String name) {
        if (name.length() > MAX_NAME_LENGTH)
            this.name = name.substring(0, MAX_NAME_LENGTH);
        else
            this.name = name;

        seh.postSampleEvent(new SampleNameChangeEvent(this, sample));
    }

    public int compareTo(Object o) {
        if (o instanceof SampleObject)
            return sample.compareTo(((SampleObject) o).sample);
        return sample.compareTo(o);
    }

    public void zDispose() {
        seh = null;
        name = null;
        sample = null;
    }
}


