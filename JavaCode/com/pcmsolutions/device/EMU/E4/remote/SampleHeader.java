package com.pcmsolutions.device.EMU.E4.remote;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 11:04:29
 */
public interface SampleHeader extends Serializable{
    public byte getBitsPerWord();           //

    public byte getNumChannels();      // 1 or 2 usually

    public byte getLoopControl();           // Specifies if and how the defined loop between LoopStart and LoopEnd should be played

    public int getPeriodInNS();    // 10^9 % period = sample frequency

    public int getLengthInSampleFrames();   // Number of frames in sample (one sample frame's elementCount is ( NumberOfChannels*BitsPerWord)/8 Bytes)

    public int getLoopStart();              // sample frame where sample starts

    public int getLoopEnd();                // sample frame where sample ends
}
