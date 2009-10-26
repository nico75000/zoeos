/*
 * RemoteHelper.java
 *
 * Created on October 28, 2002, 8:51 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.SysexParsingHelper;
import com.pcmsolutions.system.IntPool;

/**
 *
 * @author  pmeehan
 */
class SysexHelper {

    /** Creates a new instance of RemoteHelper */
    private SysexHelper() {
    }

    public static void main(String[] args) {
        byte[] arr1 = DataOut((short) 270);
        System.out.println(arr1[0]);
        System.out.println(arr1[1]);
        byte[] arr2 = {0x02, 0x05};
        System.out.println(LongDataIn_int((byte) 1, (byte) 1, (byte) 1, (byte) 1));
    }

    public static int LongDataIn_int(byte b1, byte b2, byte b3, byte b4) {
        int lw = DataIn_int(b1, b2);
        int hw = DataIn_int(b3, b4);
        //int test = hw << 14;
        return lw + (hw << 14);
    }

    public static byte[] UnsignedLongDataOut_int(int i) {
        byte b1 = (byte) ((i << 25) >> 25);
        if (b1 < 0)
            b1 += 128;
        byte b2 = (byte) ((i << 18) >> 25);
        if (b2 < 0)
            b2 += 128;
        byte b3 = (byte) ((i << 11) >> 25);
        if (b3 < 0)
            b3 += 128;
        byte b4 = (byte) ((i << 4) >> 25);
        if (b4 < 0)
            b4 += 128;

        return new byte[]{b1, b2, b3, b4};
    }

    public static byte[] LongDataOut_int(int i) {
        byte b1 = (byte) ((i << 25) >> 25);
        byte b2 = (byte) ((i << 18) >> 25);
        byte b3 = (byte) ((i << 11) >> 25);
        byte b4 = (byte) ((i << 4) >> 25);
        return new byte[]{b1, b2, b3, b4};
    }

    public static int UnsignedLongDataIn_int(byte b1, byte b2, byte b3, byte b4) {
        int lw = UnsignedDataIn_int(b1, b2);
        int hw = UnsignedDataIn_int(b3, b4);
        return lw + (hw << 14);
    }

    public static byte[] DataOut(Integer value) {
        return DataOut(value.intValue());
    }

    // prepare 14 bit value for output as 2 bytes
    public static byte[] DataOut(int value) {
        byte[] out = new byte[2];
        out[0] = (byte) (value & 127);
        out[1] = (byte) ((value & 0x3f80) >> 7);
        return out;
    }

    public static void DataOut(byte[] out, int pos, Integer value) {
        if (out.length < 2)
            throw new java.lang.IllegalArgumentException();
        out[pos] = (byte) (value.shortValue() & 127);
        out[pos + 1] = (byte) ((value.shortValue() & 0x3f80) >> 7);
    }

    public static Integer DataIn(byte[] data) {
        return DataIn(data, 0);
    }

    // convert incoming 2 bytes to 14 bit value
    public static Integer DataIn(byte[] data, int offset) {
        if (data.length < offset + 2)
            throw new java.lang.IllegalArgumentException();
        return DataIn(data[offset], data[offset + 1]);
    }

    public static Integer UnsingedDataIn(byte[] data) {
        return UnsingedDataIn(data, 0);
    }

    // convert incoming 2 bytes to 14 bit value
    public static Integer UnsingedDataIn(byte[] data, int offset) {
        if (data.length < offset + 2)
            throw new java.lang.IllegalArgumentException();
        return UnsignedDataIn(data[offset], data[offset + 1]);
    }

    public static Integer DataIn(byte inLsb, byte inMsb) {
        short lsb = (short) (inLsb & 127);
        short msb = (short) (((short) inMsb << 7) + ((short) (inLsb & 128) << 1));
        short rv = (short) (msb + lsb);
        if (rv > 8192) // 2 pow 13
            rv -= 16384;
        return IntPool.get(rv);
    }

    public static Integer UnsignedDataIn(byte inLsb, byte inMsb) {
        short lsb = (short) (inLsb & 127);
        short msb = (short) (((short) inMsb << 7) + ((short) (inLsb & 128) << 1));
        short rv = (short) (msb + lsb);
        return IntPool.get(rv);
    }

    public static int DataIn_int(byte inLsb, byte inMsb) {
        short lsb = (short) (inLsb & 127);
        short msb = (short) (((short) inMsb << 7) + ((short) (inLsb & 128) << 1));
        short rv = (short) (msb + lsb);
        if (rv > 8192) // 2 pow 13
            rv -= 16384;
        return rv;
    }

    public static int UnsignedDataIn_int(byte inLsb, byte inMsb) {
        short lsb = (short) (inLsb & 127);
        short msb = (short) (((short) inMsb << 7) + ((short) (inLsb & 128) << 1));
        short rv = (short) (msb + lsb);
        return rv;
    }

    public static int DataIn_int(byte[] data) {
        if (data.length < 2)
            throw new java.lang.IllegalArgumentException();
        return DataIn_int(data[0], data[1]);
    }

    public static int UnsignedDataIn_int(byte[] data) {
        if (data.length < 2)
            throw new java.lang.IllegalArgumentException();
        return UnsignedDataIn_int(data[0], data[1]);
    }

    public static StringBuffer StringIn(byte[] data, short pos, short length) throws ArrayIndexOutOfBoundsException {
        return SysexParsingHelper.StringIn(data, pos, length);
    }

    public static byte[] StringOut(StringBuffer name) {
        return SysexParsingHelper.StringOut(name);
    }
}
