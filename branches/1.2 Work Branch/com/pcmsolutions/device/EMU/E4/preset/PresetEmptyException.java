/*
 * NoSuchPresetException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;

/**
 *
 * @author  pmeehan
 */
public class PresetEmptyException extends PresetException {

    public PresetEmptyException(Integer preset) {
        super(preset, DeviceContext.EMPTY_PRESET);
    }

    public PresetEmptyException(Integer preset, String name) {
        super(preset, name);
    }

    public PresetEmptyException(Integer preset, String name, String msg) {
        super(preset, name, msg);
    }
}
