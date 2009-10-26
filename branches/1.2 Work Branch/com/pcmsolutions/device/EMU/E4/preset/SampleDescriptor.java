package com.pcmsolutions.device.EMU.E4.preset;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Sep-2003
 * Time: 22:57:51
 * To change this template use Options | File Templates.
 */
public interface SampleDescriptor extends Serializable {
    public byte getBitsPerWord();           //

    public byte getNumChannels();      // 1 or 2 usually

    public byte getLoopControl();           // Specifies if and how the defined loop between LoopStart and LoopEnd should be played

    public int getPeriodInNS();    // 10^9 % period = sample frequency

    public int getLengthInSampleFrames();   // Number of frames in sample (one sample frame's elementCount is ( NumberOfChannels*BitsPerWord)/8 Bytes)

    public int getLoopStart();              // sample frame where sample starts

    public int getLoopEnd();                // sample frame where sample ends

    public short getPitch();                  // (0..127) 60 is middle C

    public short getPitchFraction();          // Specifies the fine tuning of the sample, measured in cents upward

    public int getSizeInBytes();

    public String getFormattedSize();

    public double getSampleRateInKhz();

    public String getFormattedSampleRateInKhz();

    public String getFormattedDurationInSeconds();

    public String getChannelDescription();
}
