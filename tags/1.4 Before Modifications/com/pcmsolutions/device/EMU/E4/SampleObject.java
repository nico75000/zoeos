package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.sample.SampleNameChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleNewEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleCopyEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleNewEvent;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.device.EMU.database.events.content.ContentEventHandler;
import com.pcmsolutions.system.Nameable;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.gui.ProgressCallback;

import javax.sound.sampled.AudioFileFormat;
import java.io.Serializable;
import java.io.File;

class SampleObject implements DatabaseSample, Nameable, ZDisposable, Comparable, Serializable {
    private transient final int MAX_NAME_LENGTH = 16;
    private Integer sample;
    private String name;
    private ContentEventHandler ceh;
    private SampleDescriptor sampleDescriptor;
    private String summary;

    // copy constructor same sample database
    public void init(SampleObject src, Integer sample) {
        init(src, sample, src.ceh);
    }

    public void init(Integer sample, String name, ContentEventHandler ceh, SampleDescriptor sampleDescriptor) {
        this.sample = sample;
        this.name = name;
        this.ceh = ceh;
        setSampleDescriptor(sampleDescriptor);
    }

    public void init(SampleObject src, Integer sample, ContentEventHandler ceh) {
        this.ceh = ceh;
        this.name = src.name;
        this.sample = sample;
        setSampleDescriptor(src.sampleDescriptor);
    }


    /*
    public void initDrop(IsolatedSample is, Integer sample, String name, ContentEventHandler ceh, ProgressCallback prog) {
        this.ceh = ceh;
        this.name = name;
        this.sample = sample;
        SampleNewEvent sne = new SampleNewEvent(this, getIndex(), is, name, prog);
        ceh.postEvent(sne, true);        
        setSampleDescriptor(sne.getSampleDescriptor());
    }
    */

    public void initCopy(SampleObject src, Integer sample, String name, ContentEventHandler ceh) {
        this.ceh = ceh;
        this.name = name;
        this.sample = sample;
        SampleCopyEvent sce = new SampleCopyEvent(this, src.getIndex(), getIndex(), name);
        ceh.postEvent(sce, true);
        setSampleDescriptor(src.getSampleDescriptor());
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

    public void setSampleNumber(Integer sample) {
        this.sample = sample;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.length() > MAX_NAME_LENGTH)
            this.name = name.substring(0, MAX_NAME_LENGTH);
        else
            this.name = name;

        ceh.postEvent(new SampleNameChangeEvent(this, sample, this.name));
    }

    public int compareTo(Object o) {
        if (o instanceof DatabaseSample)
            return sample.compareTo(((DatabaseSample) o).getIndex());
        if (o instanceof Integer)
            return sample.compareTo((Integer) o);
        return 0;
    }

    public void zDispose() {
        ceh = null;
    }

    public Integer getIndex() {
        return sample;
    }

    public IsolatedSample getIsolatedSample(AudioFileFormat.Type format) {
        return null;
    }

    public IsolatedSample getIsolatedSample(File f, AudioFileFormat.Type format) {
        return null;
    }
}


