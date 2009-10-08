package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.SampleRetrievalInfo;

import java.io.File;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 25-Sep-2003
 * Time: 05:05:17
 * To change this template use Options | File Templates.
 */
public interface SampleMediator extends Serializable{

    // public File retrieveSample(Integer sample, File fn, AudioFileFormat.Type format, boolean overwrite) throws SampleMediationException;

    // public File retrieveSample(Integer sample, File fn, AudioFileFormat.Type format,boolean overwrite,  boolean endOfProcedure) throws SampleMediationException;

    public File retrieveSample(SampleRetrievalInfo sri) throws SampleMediationException;

    public void sendSample(Integer sample, File fn, String sampleName) throws SampleMediationException;

    public void sendSampleMulti(File fn, Integer[] destSamples, String[] destNames) throws SampleMediationException;

    public void copySample(IsolatedSample is, Integer[] destSamples, String[] destNames) throws SampleMediationException, IsolatedSampleUnavailableException;

    public void copySample(Integer srcSample, Integer[] destSamples, String[] destNames) throws SampleMediationException;

    public void sendSample(Integer sample, IsolatedSample is, String sampleName) throws SampleMediationException, IsolatedSampleUnavailableException;

    //public SampleDescriptor getSampleDescriptor(Integer sample) throws SampleMediationException;

    public class SampleMediationException extends Exception {
        public SampleMediationException(String message) {
            super(message);
        }
    };
}
