/*
 * IllegalParameterId.java
 *
 * Created on January 2, 2003, 7:41 PM
 */

package com.pcmsolutions.device.EMU.E4.multimode;

import com.pcmsolutions.device.EMU.DeviceException;

/**
 *
 * @author  pmeehan
 */
public class IllegalMultimodeChannelException extends DeviceException {
    Integer channel;

    public IllegalMultimodeChannelException(Integer mc) {
        super("Illegal midi channel");
        this.channel = mc;
    }

    public Integer getChannel() {
        return channel;
    }
}
