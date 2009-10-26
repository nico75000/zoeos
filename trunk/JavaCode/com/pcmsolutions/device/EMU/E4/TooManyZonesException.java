/*
 * TooManyZonesException.java
 *
 * Created on January 4, 2003, 5:38 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.PresetException;

/**
 *
 * @author  pmeehan
 */
class TooManyZonesException extends PresetException {
    public TooManyZonesException() {
        super("too many zones");
    }
}
