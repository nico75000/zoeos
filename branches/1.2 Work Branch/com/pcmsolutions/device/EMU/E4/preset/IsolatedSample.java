package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.system.ZDisposable;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 21-Sep-2003
 * Time: 01:33:37
 * To change this template use Options | File Templates.
 */
public interface IsolatedSample extends ZDisposable {
    public String getName();

    public Integer getOriginalIndex();

    public boolean isROMSample();

    public File getLocalFile();

    public AudioFileFormat.Type getFormatType() throws IOException, UnsupportedAudioFileException;

    public void ZoeAssert() throws IsolatedSampleUnavailableException;

    public void setLocalFile(File f, boolean moveExisting);
}
