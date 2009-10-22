package org.tritonus.zuonics.sampled.aiff;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;

interface AiffSSNDChunk extends AbstractAudioChunk{
    int getOffset();
    int getBlockSize();
    int getDataLength();
}
