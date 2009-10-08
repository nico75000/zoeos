/*
 * NoSuchZoneException.java
 *
 * Created on January 4, 2003, 4:57 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class NoSuchZoneException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>NoSuchZoneException</code> without detail message.
     */
    public NoSuchZoneException() {
    }


    /**
     * Constructs an instance of <code>NoSuchZoneException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchZoneException(String msg) {
        super(msg);
    }
}
