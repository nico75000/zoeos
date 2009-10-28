/*
 * SysexParsing.java
 *
 * Created on October 31, 2002, 2:22 AM
 */

package com.pcmsolutions.comms;

/**
 *
 * @author  pmeehan
 */
public class SysexParsingHelper {

    /** Creates a new instance of SysexParsing */
    private SysexParsingHelper() {
    }

    public static boolean isNonRealtimeSysex(byte[] msgData) {
        if (msgData[0] == Codes.BOX && msgData[1] == Codes.NON_REALTIME_ID)
            return true;
        return false;
    }

    public static boolean isIdentityReply(byte[] msgData) {
        if (msgData[4] == Codes.GENERAL_INFO_ID && msgData[5] == Codes.IDENTITY_REPLY_ID)
            return true;
        return false;
    }

    public static StringBuffer StringIn(byte[] data, short pos, short length) throws ArrayIndexOutOfBoundsException {
        StringBuffer name = new StringBuffer();
        for (int n = pos; n < pos + length; n++) {
            name.append((char) data[n]);
        }
        return name;
    }

    public static byte[] StringOut(StringBuffer name) {
        int len = name.length();
        byte[] out = new byte[16];
        for (int n = 0; n < 16; n++) {
            if (n > len - 1)
                out[n] = (byte) 0;
            else
                out[n] = (byte) name.charAt(n);
        }
        return out;
    }

}
