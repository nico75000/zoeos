package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-Oct-2003
 * Time: 10:54:37
 * To change this template use Options | File Templates.
 */
class Impl_SerializableIsolatedSample implements IsolatedSample, Serializable {
    private String name;
    private File localFile;
    private Integer sample;

    public Impl_SerializableIsolatedSample(Integer sample, File localCopy, String name) {
        this.sample = sample;
        this.localFile = localCopy;
        this.name = name;
    }

    public Impl_SerializableIsolatedSample(IsolatedSample is) {
        this.sample = is.getOriginalIndex();
        this.localFile = is.getLocalFile();
        this.name = is.getName();
    }

    public boolean isROMSample() {
        return sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOriginalIndex() {
        return sample;
    }

    public File getLocalFile() {
        return localFile;
    }

    public AudioFileFormat.Type getFormatType() throws IOException, UnsupportedAudioFileException {
        return AudioSystem.getAudioFileFormat(localFile).getType();
    }

    public void ZoeAssert() throws IsolatedSampleUnavailableException {
        if (!isROMSample()) {
            if ((localFile != null && localFile.exists()))
                return;
            throw new IsolatedSampleUnavailableException("File not available");
        }

    }

    /* public SampleDescriptor getSampleDescriptor() throws IsolatedSampleUnavailableException {
         if (!isROMSample) {
             if (!secured)
                 secure();
             return sd;
         } else
             return null;
     }
      */
    public void setLocalFile(File f, boolean moveExisting) {
        if (localFile != null && moveExisting)
            localFile.renameTo(f);
        localFile = f;
    }

    public void zDispose() {
        if (localFile != null)
            localFile.delete();
    }
}
