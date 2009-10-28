package org.tritonus.zuonics.sampled.wave;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;

import javax.sound.sampled.AudioFormat;

interface WaveFmtChunk extends AbstractAudioChunk{
    AudioFormat.Encoding getEncoding();
    int getSampleSizeInBits();
    int getChannelCount();
    int getFrameSize();
    float getFrameRate();
}
