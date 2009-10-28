package com.pcmsolutions.smdi;

import javax.sound.sampled.AudioInputStream;

/**
 * User: paulmeehan
 * Date: 27-Feb-2004
 * Time: 03:38:23
 */
public interface SMDISendInstance extends SMDITransferInstance {

    public AudioInputStream getAudioInputStream();

    public String getSampleName();
}
