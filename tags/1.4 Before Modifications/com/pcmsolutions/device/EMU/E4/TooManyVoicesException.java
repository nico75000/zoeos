/*
 * TooManyVoicesException.java
 *
 * Created on January 4, 2003, 4:56 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.PresetException;

/**
 *
 * @author  pmeehan
 */
class TooManyVoicesException extends PresetException {
    public TooManyVoicesException() {
        super("too many voices");
    }
}
