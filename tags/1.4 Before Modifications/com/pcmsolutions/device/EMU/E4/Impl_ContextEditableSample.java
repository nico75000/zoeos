package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.E4.zcommands.sample.LoadContextSamplesZMTC;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.TempFileManager;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.callback.CompleteProgressOnCancelledCallback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;

class Impl_ContextEditableSample extends Impl_ContextBasicEditableSample implements ContextEditableSample, IconAndTipCarrier, Comparable {

    static {
        SampleClassManager.addSampleClass(Impl_ContextEditableSample.class, null);
    }

    public Impl_ContextEditableSample(SampleContext pc, Integer sample) {
        super(pc, sample);
    }

    public void performDefaultAction() {
        LoadContextSamplesZMTC lcs = new LoadContextSamplesZMTC();
        try {
            lcs.setTargets(Impl_ContextEditableSample.this);
            lcs.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ReadableSample getMostCapableNonContextEditableSampleDowngrade() {
        return new Impl_ContextBasicEditableSample(sc, sample);
    }

    /*public void sendSample(Integer destSample) throws DeviceException, EmptyException, IsolatedSampleUnavailableException {
        try {
            sc.sendSample(sample, new Integer[]{destSample}, null);
        } catch (NoSuchContextException e) {
            throw new DeviceException(sample);
        }
    } */

    public void copySample(Integer[] destSamples, ProgressCallback prog) throws SampleException {
        try {
            sc.copy(sample, destSamples, prog).post();
        } catch (ResourceUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        } finally {
        }
    }

    /*
    public File retrieveCustomLocalCopy(SampleDownloadDescriptor sri, ProgressCallback prog) throws  EmptyException, SampleRetrievalException {
        if (!sri.getIndex().equals(this.getIndex()))
            throw new IllegalArgumentException("incorrect sample requested");
        try {
            return sc.retrieveCustomLocalCopy(sri, prog);
        } catch (NoSuchContextException e) {
            throw new DeviceException(sample);
        }
    }
    */
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

    public ZCommand[] getZCommands(Class markerClass) {
        return ContextEditableSample.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ContextEditableSample.cmdProviderHelper.getSupportedMarkers();
    }

    public IsolatedSample getIsolated(SampleDownloadDescriptor sdd) throws EmptyException, SampleException {
        try {
            return sc.getIsolated(sample, sdd);
        } catch (ContentUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public IsolatedSample getIsolated(AudioFileFormat.Type format) throws EmptyException, SampleException {
        try {
            return sc.getIsolated(sample, format);
        } catch (ContentUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public IsolatedSample getIsolated(String fileName, AudioFileFormat.Type format) throws EmptyException, SampleException {
        try {
            return sc.getIsolated(SampleDownloadDescriptorFactory.getDownloadToSpecificFile(sample, new File(TempFileManager.getTempDirectory(), fileName), format, true));
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public void newContent(IsolatedSample is, String name, ProgressCallback prog) throws SampleException {
        newContent(is, name, prog, null);
    }

    public void newContent(IsolatedSample is, String name, ProgressCallback prog, Callback cb) throws SampleException {
        try {
            sc.newContent(is, sample, name, prog).post(new CompleteProgressOnCancelledCallback(prog, cb));
        } catch (ResourceUnavailableException e) {
            prog.updateProgress(1);
            throw new SampleException(sample, e.getMessage());
        }
    }

    public SampleDescriptor getSampleDescriptor() throws EmptyException, SampleException {
        try {
            return sc.getSampleDescriptor(sample);
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }
}

