package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.system.ZDisposable;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 06-Dec-2003
 * Time: 10:33:44
 * To change this template use Options | File Templates.
 */
public interface SampleRetrievalInfo extends ZDisposable{
    public Integer getSample();

    public File getFile();

    public String getNamingMode();

    public AudioFileFormat.Type getFormat();

    public boolean isOverwriting();

   // public boolean isEndOfAProcedure();

    public boolean isApplyingExtension();
}
