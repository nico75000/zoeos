/*
 * MidiCodes.java
 *
 * Created on October 28, 2002, 7:11 PM
 */

package com.pcmsolutions.comms;

/**
 *
 * @author  pmeehan
 */
public class Codes {
    // sysex codes
    public static byte BOX = (byte) 0xF0;
    public static byte EOX = (byte) 0xF7;
    public static byte NON_REALTIME_ID = (byte) 0x7E;
    public static byte REALTIME_ID = (byte) 0x7F;
    public static byte GENERAL_INFO_ID = (byte) 0x06;
    public static byte IDENTITY_REPLY_ID = (byte) 0x02;


    // E-MU specific codes
    public static byte EMU_E4_ID = (byte) 0x21;
    public static byte EMU_EDITOR_ID = (byte) 0x55;
    public static byte EMU_E4_DEVICE_FAMILY_ID_LSB = (byte) 0x01;
    public static byte EMU_E4_DEVICE_FAMILY_ID_MSB = (byte) 0x04;

    // manufacturer ids
    public static byte MANUFACTURER_ID_EMU = 0x18;

    //private Codes() {
    //}
}


