/*
 * DeviceException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.device.EMU.database.ContextException;


/**
 * @author pmeehan
 */
public class PresetException extends ContextException {
    Integer preset = IntPool.minus_one;
    public PresetException(Integer preset, String message) {
        super(message);
        this.preset = preset;
    }

    public PresetException(String message) {
        super(message);
    }

    public Integer getPreset() {
        return preset;
    }
}
