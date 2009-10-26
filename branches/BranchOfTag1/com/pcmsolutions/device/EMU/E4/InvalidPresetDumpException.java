/*
 * InvalidPresetDumpException.java
 *
 * Created on February 10, 2003, 5:02 AM
 */

package com.pcmsolutions.device.EMU.E4;

/**
 *
 * @author  pmeehan
 */
class InvalidPresetDumpException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>InvalidPresetDumpException</code> without detail message.
     */
    public InvalidPresetDumpException() {
    }


    /**
     * Constructs an instance of <code>InvalidPresetDumpException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidPresetDumpException(String msg) {
        super(msg);
    }
}
