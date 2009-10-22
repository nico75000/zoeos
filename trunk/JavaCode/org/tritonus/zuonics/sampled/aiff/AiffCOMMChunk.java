package org.tritonus.zuonics.sampled.aiff;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;

import javax.sound.sampled.AudioFormat;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;

interface AiffCOMMChunk extends AbstractAudioChunk{
    int getNumChannels();
    int getFrameSize();
    int  getSampleSize();
    float getSampleRate();
    AudioFormat.Encoding getEncoding();
}
