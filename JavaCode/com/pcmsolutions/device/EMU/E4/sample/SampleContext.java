/*
 * PresetContext.java
 *
 * Created on January 3, 2003, 9:10 PM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.database.Context;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.tasking.Ticket;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  pmeehan
 */
public interface SampleContext extends Context<ReadableSample, SampleContext, SampleListener, IsolatedSample>{

    public DeviceParameterContext getDeviceParameterContext() throws DeviceException;

    public DeviceContext getDeviceContext();

    public PresetContext getRootPresetContext();

    // EVENTS
    // returns List of ContextEditableSample
    public List<ContextEditableSample> getContextEditableSamples() throws DeviceException;

    // returns List of ReadableSample   or better ( e.g FLASH/ROM and out of context samples returned as ReadableSample)
    public List<ReadableSample> getDatabaseSamples() throws DeviceException;

    public String getSampleSummary(Integer sample) throws DeviceException;

    // returns null for ROM samples or non-SMDI linked devices
    public SampleDescriptor getSampleDescriptor(Integer sample) throws DeviceException, EmptyException, ContentUnavailableException;

    public ContextReadableSample getContextSample(Integer sample) throws DeviceException;

    public List<ContextReadableSample> getContextSamples() throws DeviceException;

    public ReadableSample getReadableSample(Integer sample) throws DeviceException;

    public ContextEditableSample getEditableSample(Integer sample) throws DeviceException;

    public Map<Integer, String> getContextUserNamesMap() throws DeviceException;

    public IsolatedSample getIsolated(SampleDownloadDescriptor sdd) throws DeviceException, ContentUnavailableException, EmptyException;

    public Ticket newContent(IsolatedSample is, Integer sample, String name, ProgressCallback prog) ;

    public Ticket copy(Integer srcIndex, Integer[] destIndexes, ProgressCallback prog);
}
