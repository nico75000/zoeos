/*
 * PresetNotInContextException.java
 *
 * Created on January 3, 2003, 10:06 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class PresetNotAvailableException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>PresetNotInContextException</code> without detail message.
     */
    public PresetNotAvailableException() {
    }


    /**
     * Constructs an instance of <code>PresetNotInContextException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PresetNotAvailableException(String msg) {
        super(msg);
    }
}
