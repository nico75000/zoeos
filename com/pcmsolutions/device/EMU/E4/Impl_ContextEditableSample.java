package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextEditableSampleZCommandMarker;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZUtilities;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;

class Impl_ContextEditableSample extends Impl_ContextBasicEditableSample implements ContextEditableSample, IconAndTipCarrier, Comparable {
    private static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextEditableSampleZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.CopyContextSampleZC;com.pcmsolutions.device.EMU.E4.zcommands.CopyRangeContextSampleZC;com.pcmsolutions.device.EMU.E4.zcommands.CopyBlockContextSamplesZMTC;com.pcmsolutions.device.EMU.E4.zcommands.SaveContextSamplesZMTC;com.pcmsolutions.device.EMU.E4.zcommands.LoadContextSamplesZMTC;com.pcmsolutions.device.EMU.E4.zcommands.NewSamplePackageZMTC;com.pcmsolutions.device.EMU.E4.zcommands.LoadSamplePackageZMTC");

    static {
        SampleClassManager.addSampleClass(Impl_ContextEditableSample.class, null);
    }

    public Impl_ContextEditableSample(SampleContext pc, Integer sample) {
        super(pc, sample);
    }

    public ReadableSample getMostCapableNonContextEditableSampleDowngrade() {
         return new Impl_ContextBasicEditableSample(sc, sample);
     }

    public void copySample(Integer destSample) throws NoSuchSampleException, SampleEmptyException, IsolatedSampleUnavailableException {
        try {
            sc.copySample(sample, new Integer[]{destSample});
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public void copySample(Integer[] destSamples) throws NoSuchSampleException, SampleEmptyException, IsolatedSampleUnavailableException {
        for (int i = 0; i < destSamples.length; i++)
            copySample(destSamples[i]);
    }

    public File retrieveCustomLocalCopy(SampleRetrievalInfo sri) throws NoSuchSampleException, SampleEmptyException, SampleRetrievalException {
        if ( !sri.getSample().equals(this.getSampleNumber()))
            throw new IllegalArgumentException("incorrect sample requested");
        try {
            return sc.retrieveCustomLocalCopy(sri);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public boolean equals(Object o) {
        Impl_ContextEditableSample p;
        if (o instanceof Impl_ContextEditableSample) {
            p = (Impl_ContextEditableSample) o;
            if (p.sample.equals(sample) && p.sc.equals(sc))
                return true;
        }
        /*else    // try and compare using just sample number
            if ( o instanceof Integer ){
                if ( o.equals(sample))
                    return true;
            }
        */
        return false;
    }

    public ZCommand[] getZCommands() {
        return ZUtilities.concatZCommands(super.getZCommands(), cmdProviderHelper.getCommandObjects(this));
    }

    public IsolatedSample getIsolated(AudioFileFormat.Type format) throws NoSuchSampleException, SampleEmptyException {
        try {
            return sc.getIsolatedSample(sample, format);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public IsolatedSample getIsolated(String fileName, AudioFileFormat.Type format) throws NoSuchSampleException, SampleEmptyException {
        try {
            return sc.getIsolatedSample(sample, fileName, format);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public void newSample(IsolatedSample is, String name) throws IsolatedSampleUnavailableException, NoSuchSampleException {
        try {
            sc.newSample(is, sample, name);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }
}

