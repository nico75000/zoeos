/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextEditableSampleZCommandMarker;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.callback.Callback;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;


/**
 *
 * @author  pmeehan
 */

public interface ContextEditableSample extends ContextBasicEditableSample, Comparable {
    final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextEditableSampleZCommandMarker.class, ContextBasicEditableSample.cmdProviderHelper);

    public IsolatedSample getIsolated(SampleDownloadDescriptor sdd) throws EmptyException, SampleException;

    public IsolatedSample getIsolated(AudioFileFormat.Type format) throws EmptyException, SampleException;

    public IsolatedSample getIsolated(String fileName, AudioFileFormat.Type format) throws  EmptyException, SampleException;

    public void newContent(IsolatedSample is, String name, ProgressCallback prog) throws IsolatedSampleUnavailableException, SampleException, ContentUnavailableException;

    public void newContent(IsolatedSample is, String name, ProgressCallback prog, Callback cb) throws SampleException;

    public SampleDescriptor getSampleDescriptor() throws EmptyException, SampleException;

    public void copySample(Integer[] destSamples, ProgressCallback prog) throws SampleException;
}
