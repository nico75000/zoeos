/*
 * NoSuchZoneException.java
 *
 * Created on January 4, 2003, 4:57 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.PresetException;

/**
 *
 * @author  pmeehan
 */
class NoSuchZoneException extends PresetException {
    public NoSuchZoneException() {
        super("no such zone");
    }
}
