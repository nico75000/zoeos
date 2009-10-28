package com.pcmsolutions.smdi;

import javax.sound.sampled.AudioFileFormat;
import java.io.OutputStream;
import java.util.Map;

/**
 * User: paulmeehan
 * Date: 27-Feb-2004
 * Time: 03:38:23
 */
public interface SMDIRecvInstance extends SMDITransferInstance {
    public OutputStream getOutputStream();
    public AudioFileFormat.Type getFileType();
    public Map<String,Object> getPropertiesForSampleHeader(SmdiSampleHeader hdr);
}
