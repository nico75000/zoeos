/*
 * NoSuchPresetException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;


/**
 *
 * @author  pmeehan
 */
public class NoSuchPresetException extends PresetException {

    public NoSuchPresetException(Integer preset) {
        super(preset);
    }

    public NoSuchPresetException(Integer preset, String name) {
        super(preset, name);
    }

    public NoSuchPresetException(Integer preset, String name, String msg) {
        super(preset, name, msg);
    }
}
