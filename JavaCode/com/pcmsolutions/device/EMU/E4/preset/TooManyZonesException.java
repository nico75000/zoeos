/*
 * TooManyZonesException.java
 *
 * Created on January 4, 2003, 5:38 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class TooManyZonesException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>TooManyZonesException</code> without detail message.
     */
    public TooManyZonesException() {
    }


    /**
     * Constructs an instance of <code>TooManyZonesException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TooManyZonesException(String msg) {
        super(msg);
    }
}
