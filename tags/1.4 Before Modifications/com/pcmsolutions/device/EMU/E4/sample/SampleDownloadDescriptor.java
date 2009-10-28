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
public interface SampleDownloadDescriptor extends ZDisposable {
    File getFile();

    String getNamingMode();

    AudioFileFormat.Type getFormat();

    boolean isOverwriting();

    boolean isApplyingExtension();

    public Integer getIndex();
}
