package com.pcmsolutions.smdi;

import com.pcmsolutions.device.EMU.E4.remote.SampleHeader;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 08-Mar-2003
 * Time: 15:19:49
 * To change this template use Options | File Templates.
 */
public interface SmdiSampleHeader extends SampleHeader, Serializable {
    public boolean isDoesExist();

    public byte getLoopControl();           // Specifies if and how the defined loop between LoopStart and LoopEnd should be played

    public byte getNameLengthInBytes();

    public short getPitch();                  // (0..127) 60 is middle C

    public short getPitchFraction();          // Specifies the fine tuning of the sample, measured in cents upward

    // from the semitone. 60.00 is exactly middle C, 60.32768 is 50 cents above middle C
    // In the case that the two pitch values are unavailable, a default of 60.00 should be used.
    public String getName();                   // name of sample
}
