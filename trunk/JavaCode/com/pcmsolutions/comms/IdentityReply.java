/*
 * IdentityResponseMessage.java
 *
 * Created on October 30, 2002, 8:39 PM
 */

package com.pcmsolutions.comms;

import com.pcmsolutions.system.Readable;
import com.pcmsolutions.system.ZUtilities;

import javax.sound.midi.MidiDevice;
import java.io.ByteArrayInputStream;

/**
 *
 * @author  pmeehan
 */
public class IdentityReply extends Sysex implements Filterable, Readable {
    protected static final int minLength = 15;

    /** Creates a new instance of IdentityResponseMessage */
    public IdentityReply() {
        super();
    }

    protected IdentityReply(byte[] data, MidiDevice.Info source) {
        super(data, source);
    }

    public String toString() {
        return "Sysex IRM (bytes = " + getByteString() + ")";
    }

    public String toReadable() {
        return toString();
    }

    public String getByteString() {
        return ZUtilities.getByteString(data);
        /*   StringBuffer sb = new StringBuffer();
        for (int n = 0; n < data.length - 1; n++) {
            sb.append(data[n]);
            sb.append("_");
        }
        sb.append(data[data.length - 1]);
        return sb.toString();
        */
    }

    public byte getDeviceId() {
        return data[2];
    }

    public String getSoftwareRevision() {
        return SysexParsingHelper.StringIn(data, (short) 10, (short) 4).toString();
    }

    public byte[] getDeviceFamilyMemberCode() {
        return new byte[]{data[8], data[9]};
    }

    public byte[] getDeviceFamilyCode() {
        return new byte[]{data[6], data[7]};
    }

    public byte getManufacturerID() {
        return data[5];
    }

    public Object filter(ByteStreamable o) {
        Object robj = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[minLength];
        int res = is.read(data, 0, minLength);
        if (res == minLength
                && data[0] == Codes.BOX
                && data[1] == Codes.NON_REALTIME_ID
                && data[3] == Codes.GENERAL_INFO_ID
                && data[4] == Codes.IDENTITY_REPLY_ID
        ) {
            robj = new IdentityReply(data, null);
        }

        return robj;
    }
}