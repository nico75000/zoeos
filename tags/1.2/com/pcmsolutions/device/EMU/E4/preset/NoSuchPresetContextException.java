/*
 * NoSuchPresetContextException.java
 *
 * Created on January 9, 2003, 6:28 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class NoSuchPresetContextException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>NoSuchPresetContextException</code> without detail message.
     */
    public NoSuchPresetContextException() {
    }


    /**
     * Constructs an instance of <code>NoSuchPresetContextException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchPresetContextException(String msg) {
        super(msg);
    }
}
