/*
 * ReadOnlyPresetException.java
 *
 * Created on January 6, 2003, 5:41 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class ReadOnlyPresetException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>ReadOnlyPresetException</code> without detail message.
     */
    public ReadOnlyPresetException() {
    }


    /**
     * Constructs an instance of <code>ReadOnlyPresetException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ReadOnlyPresetException(String msg) {
        super(msg);
    }
}
