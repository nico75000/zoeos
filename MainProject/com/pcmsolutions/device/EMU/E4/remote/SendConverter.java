package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.smdi.SmdiUnsupportedConversionException;
import com.pcmsolutions.system.audio.AudioConversionException;
import com.pcmsolutions.system.audio.AudioConverter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 11:39:37
 */
class SendConverter {
    static AudioInputStream prepareAudioStream(AudioInputStream ais, float maxRate) throws SmdiUnsupportedConversionException, AudioConversionException {
        AudioFormat af = ais.getFormat();
        if (af.getChannels() > 2)
            throw new SmdiUnsupportedConversionException("Too many channels in audio data");

        float rate = af.getSampleRate();
        if (rate > maxRate)
            rate = maxRate;
        return AudioConverter.convertStream(ais, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, af.getChannels(), /*af.getFrameSize()*/af.getChannels() * 2, af.getFrameRate(), true, af.properties()));
    }
}

