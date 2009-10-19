/*
 * PresetContext.java
 *
 * Created on January 3, 2003, 9:10 PM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author  pmeehan
 */
public interface SampleContext {

    public String getDeviceString();

    public DeviceParameterContext getDeviceParameterContext();

    public DeviceContext getDeviceContext();

    public PresetContext getRootPresetContext();

    // EVENTS
    public void addSampleContextListener(SampleContextListener pl);

    public void removeSampleContextListener(SampleContextListener pl);

    public void addSampleListener(SampleListener pl, Integer[] samples);

    public void removeSampleListener(SampleListener pl, Integer[] samples);

    // returns Set of Integer
    public Set getSampleIndexesInContext() throws NoSuchContextException;

    // returns List of ContextReadableSample/ReadableSample ( e.g FLASH/ROM samples returned as ReadableSample)
    public List getContextSamples() throws NoSuchContextException;

    // returns List of ReadableSample   or better ( e.g FLASH/ROM and out of context samples returned as ReadableSample)
    public List getDatabaseSamples() throws NoSuchContextException;

    // set of integers
    public Set getDatabaseIndexes() throws NoSuchContextException;

    // returns Map of Integer -> String
    public Map getSampleNamesInContext() throws NoSuchContextException;

    // returns Map of Integer -> String
    public Map getUserSampleNamesInContext() throws NoSuchContextException;

    public boolean isSampleInContext(Integer sample);

    public int size();

    public boolean isSampleEmpty(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void expandContext(SampleContext pc, Integer[] samples) throws NoSuchContextException, NoSuchSampleException;

    public List expandContextWithEmptySamples(SampleContext pc, Integer reqd) throws NoSuchContextException;

    public List findEmptySamplesInContext(int reqd) throws NoSuchContextException;

    // looks for empties on or after beginIndex
    public List findEmptySamplesInContext(int reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException;

    public Integer firstEmptySampleInContext() throws NoSuchContextException, NoSuchSampleException;

    public Integer firstEmptySampleInDatabaseRange(Integer lowSample, Integer highSample) throws NoSuchContextException, NoSuchSampleException;

    public int numEmpties(Integer[] samples) throws NoSuchSampleException, NoSuchContextException;

    public int numEmpties(Integer lowSample, int num) throws NoSuchSampleException, NoSuchContextException;

    public SampleContext newContext(String name, Integer[] samples) throws NoSuchSampleException, NoSuchContextException;

    public void assertSampleNamed(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void assertSampleInitialized(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void release() throws NoSuchContextException;

    public boolean hasLocalCopy(Integer sample) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException;

    public File retrieveLocalCopy(Integer sample, boolean overwrite) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException, SampleRetrievalException;

    public void eraseLocalCopy(Integer sample) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException;

    public SampleDescriptor getLocalCopyHeader(Integer sample);

    public File retrieveCustomLocalCopy(SampleRetrievalInfo sri) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException, SampleRetrievalException;

    // value between 0 and 1 representing fraction of dump completed
    // value < 0 means no dump in progress
    public double getInitializationStatus(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void lockSampleRead(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void lockSampleWrite(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public void unlockSample(Integer sample);

    public boolean isSampleInitialized(Integer sample) throws NoSuchSampleException;

    public int getSampleState(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public String getSampleSummary(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public boolean isSampleWriteLocked(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public boolean isSampleWritable(Integer sample);

    public String getSampleName(Integer sample) throws NoSuchSampleException, SampleEmptyException;

    public void setSampleName(Integer sample, String name) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException;

    public void copySample(Integer srcSample, Integer[] destSamples) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException, IsolatedSampleUnavailableException;

    public void eraseSample(Integer sample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException;

    public void refreshSample(Integer sample) throws NoSuchSampleException, NoSuchContextException;

    public ContextReadableSample getContextSample(Integer sample) throws NoSuchSampleException;

    public ReadableSample getReadableSample(Integer sample) throws NoSuchSampleException;

    public ContextEditableSample getEditableSample(Integer sample) throws NoSuchSampleException;

    public IsolatedSample getIsolatedSample(Integer sample, AudioFileFormat.Type format) throws NoSuchSampleException, NoSuchContextException, SampleEmptyException;

    public IsolatedSample getIsolatedSample(Integer sample, File file, AudioFileFormat.Type format) throws NoSuchSampleException, NoSuchContextException, SampleEmptyException;

    public IsolatedSample getIsolatedSample(Integer sample, String fileName, AudioFileFormat.Type format) throws NoSuchSampleException, NoSuchContextException, SampleEmptyException;

    public void newSample(IsolatedSample is, Integer sample, String name) throws NoSuchContextException, NoSuchSampleException, IsolatedSampleUnavailableException;
}
