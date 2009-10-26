/*
 * IdentityResponseMessage.java
 *
 * Created on October 30, 2002, 8:39 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.ByteStreamable;
import com.pcmsolutions.comms.Codes;
import com.pcmsolutions.comms.IdentityReply;
import com.pcmsolutions.device.DeviceDescriptable;

import java.io.ByteArrayInputStream;

/**
 *
 * @author  pmeehan
 */
class EMU_E4_IRM extends IdentityReply implements DeviceDescriptable, java.io.Serializable {
    /** Creates a new instance of IdentityResponseMessage */
    public EMU_E4_IRM() {
        super();
    }

    protected EMU_E4_IRM(byte[] data) {
        super(data, null);
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
                && data[5] == Codes.MANUFACTURER_ID_EMU
                && data[6] == Codes.EMU_E4_DEVICE_FAMILY_ID_LSB
                && data[7] == Codes.EMU_E4_DEVICE_FAMILY_ID_MSB
        ) {
            robj = new EMU_E4_IRM(data);
        }

        return robj;
    }


    public String toReadable() {
        return (getModel() + " " + getVersion());
    }

    public String getManufacturer() {
        return "E-MU";
    }

    public String getModel() {
        byte[] x = getDeviceFamilyMemberCode();

        if (x[0] == 0x00 && x[1] == 0x05)
            return "E4";
        if (x[0] == 0x01 && x[1] == 0x05)
            return "E64";
        if (x[0] == 0x02 && x[1] == 0x05)
            return "E4X";
        if (x[0] == 0x03 && x[1] == 0x05)
            return "E64X";
        if (x[0] == 0x04 && x[1] == 0x05)
            return "E4XT";
        if (x[0] == 0x05 && x[1] == 0x05)
            return "E4X";
        if (x[0] == 0x06 && x[1] == 0x05)
            return "E6400";
        if (x[0] == 0x07 && x[1] == 0x05)
            return "E4XT Ultra";
        if (x[0] == 0x08 && x[1] == 0x05)
            return "E6400 Ultra";

        return "E4?" + "(" + Integer.toString(x[0]) + "," + Integer.toString(x[1]) + ")";
    }

    public String getVersion() {
        return super.getSoftwareRevision();
    }
}