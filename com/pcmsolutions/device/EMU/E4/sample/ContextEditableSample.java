/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;


/**
 *
 * @author  pmeehan
 */

public interface ContextEditableSample extends ContextBasicEditableSample, Comparable {

    public IsolatedSample getIsolated(AudioFileFormat.Type format) throws NoSuchSampleException, SampleEmptyException;

    public IsolatedSample getIsolated(String fileName, AudioFileFormat.Type format) throws NoSuchSampleException, SampleEmptyException;

    public void newSample(IsolatedSample is, String name) throws IsolatedSampleUnavailableException, NoSuchSampleException;

    public void copySample(Integer[] destSamples) throws NoSuchSampleException, SampleEmptyException, IsolatedSampleUnavailableException;

    // returns name of File saved to, otherwise null if not saved
   // public File retrieveCustomLocalCopy(File f,String namingMode, AudioFileFormat.Type format,  boolean overwrite,  boolean endOfProcedure) throws NoSuchSampleException, SampleEmptyException, SampleRetrievalException;
    public File retrieveCustomLocalCopy(SampleRetrievalInfo info) throws NoSuchSampleException, SampleEmptyException, SampleRetrievalException;
}
