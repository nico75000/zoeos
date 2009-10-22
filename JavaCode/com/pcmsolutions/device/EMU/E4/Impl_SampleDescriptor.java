package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.smdi.SmdiSampleHeader;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Sep-2003
 * Time: 23:48:51
 * To change this template use Options | File Templates.
 */
public class Impl_SampleDescriptor implements SampleDescriptor {
    private SmdiSampleHeader hdr;

    public Impl_SampleDescriptor(SmdiSampleHeader hdr) {
        this.hdr = hdr;
    }

    public byte getBitsPerWord()           //
    {
        return hdr.getBitsPerWord();
    }

    public byte getNumChannels()      // 1 or 2 usually
    {
        return hdr.getNumChannels();
    }

    public byte getLoopControl()           // Specifies if and how the defined loop between LoopStart and LoopEnd should be played
    {
        return hdr.getLoopControl();
    }

    public int getPeriodInNS()    // 10^9 % period = sample frequency
    {
        return hdr.getPeriodInNS();
    }

    public int getLengthInSampleFrames()   // Number of frames in sample (one sample frame's elementCount is ( NumberOfChannels*BitsPerWord)/8 Bytes)
    {
        return hdr.getLengthInSampleFrames();
    }

    public int getLoopStart()              // sample frame where sample starts
    {
        return hdr.getLoopStart();
    }

    public int getLoopEnd()                // sample frame where sample ends
    {
        return hdr.getLoopEnd();
    }

    public short getPitch()                  // (0..127) 60 is middle C
    {
        return hdr.getPitch();
    }

    public short getPitchFraction()          // Specifies the fine tuning of the sample, measured in cents upward
    {
        return hdr.getPitchFraction();
    }

    public int getSizeInBytes() {
        return hdr.getLengthInSampleFrames() * hdr.getNumChannels() * (hdr.getBitsPerWord() / 8);
    }

    public String getFormattedSize() {
        double sib = getSizeInBytes();
        if (sib < 1024)
            return (int) sib + " bytes";
        else {
            DecimalFormat df = new DecimalFormat(".##");
            if (sib < 1048576)
                return df.format(sib / 1024) + " Kb";
            else
                return df.format(sib / 1048576) + " MB";
        }
    }

    public double getSampleRateInKhz() {
        return (double) 1000000 / (double) hdr.getPeriodInNS();
    }

    private static final DecimalFormat rateFormatter = new DecimalFormat("0.##");
    public String getFormattedSampleRateInKhz() {
        return rateFormatter.format(getSampleRateInKhz()) + " Khz";
    }

    public String getFormattedDurationInSeconds() {
        DecimalFormat df = new DecimalFormat("0.###");
        return df.format(((double) hdr.getPeriodInNS() * (double) hdr.getLengthInSampleFrames()) / (double) 1000000000) + " secs";
    }

    public String getChannelDescription() {
        int chnls = hdr.getNumChannels();
        if (chnls == 1)
            return "Mono";
        else if (chnls == 2)
            return "Stereo";
        else
            return chnls + " channel";
    }

    public String getName() {
        return hdr.getName();
    }
}
