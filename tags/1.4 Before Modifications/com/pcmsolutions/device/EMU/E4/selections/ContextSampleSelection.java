package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.audio.AudioUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 19:25:16
 * To change this template use Options | File Templates.
 */
public class ContextSampleSelection extends AbstractE4Selection {
    protected ReadableSample[] readableSamples;
    protected SampleContext sampleContext;
    protected IsolatedSample[] isolatedSamples;
    protected boolean useTempNames = true;

    public ContextSampleSelection(DeviceContext d, ReadableSample[] samples, SampleContext sc) {
        super(d);
        this.readableSamples = samples;
        this.sampleContext = sc;
        //this.useTempNames = useTempNames;
    }

    public int getSampleCount() {
        return readableSamples.length;
    }

    public void setUseTempNames(boolean useTempNames) {
        this.useTempNames = useTempNames;
    }

    public Integer[] getSampleIndexes() {
        Integer[] sampleIndexes = new Integer[readableSamples.length];
        for (int i = 0; i < sampleIndexes.length; i++)
            sampleIndexes[i] = readableSamples[i].getIndex();
        return sampleIndexes;
    }

    public SampleContext getSampleContext() {
        return sampleContext;
    }

    public ReadableSample[] getReadableSamples() {
        return (ReadableSample[]) readableSamples.clone();
    }

    public IsolatedSample[] getIsolatedSamples() throws IsolatedSampleUnavailableException {
        IsolatedSample[] isoSamples = new IsolatedSample[getSampleCount()];
        for (int i = 0; i < getSampleCount(); i++)
            isoSamples[i] = getIsolatedSample(i);
        return isoSamples;
    }

    public IsolatedSample getIsolatedSample(int i) throws IsolatedSampleUnavailableException {
        if (isolatedSamples == null)
            isolatedSamples = new IsolatedSample[readableSamples.length];

        String es = null;
        if (i >= 0 && i < readableSamples.length) {
            if (isolatedSamples[i] == null && readableSamples[i] instanceof ContextEditableSample)
                try {
                    if (useTempNames)
                        isolatedSamples[i] = ((ContextEditableSample) readableSamples[i]).getIsolated(AudioUtilities.defaultAudioFormat);
                    else
                        isolatedSamples[i] = ((ContextEditableSample) readableSamples[i]).getIsolated(AudioUtilities.makeLocalSampleName(((ContextEditableSample) readableSamples[i]).getIndex(), ((ContextEditableSample) readableSamples[i]).getName(), AudioUtilities.SAMPLE_NAMING_MODE_IN), AudioUtilities.defaultAudioFormat);
                } catch (EmptyException e) {
                    es = e.getMessage();
                } catch (SampleException e) {
                    es = e.getMessage();
                }
            if (isolatedSamples[i] != null)
                return isolatedSamples[i];
        }
        throw new IsolatedSampleUnavailableException(es);
    }
}
