/*
 * NoSuchLinkException.java
 *
 * Created on January 4, 2003, 4:56 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.PresetException;

/**
 *
 * @author  pmeehan
 */
class NoSuchLinkException extends PresetException {
    public NoSuchLinkException() {
        super("no such link");
    }
}
