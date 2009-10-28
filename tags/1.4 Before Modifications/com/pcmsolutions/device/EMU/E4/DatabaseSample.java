package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.database.Content;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 04:04:59
 */
public interface DatabaseSample extends Content
 {
    SampleDescriptor getSampleDescriptor();

    String getSummary();

    String getName();

    Integer getIndex();

    IsolatedSample getIsolatedSample(AudioFileFormat.Type format);

    IsolatedSample getIsolatedSample(File f, AudioFileFormat.Type format);
}
