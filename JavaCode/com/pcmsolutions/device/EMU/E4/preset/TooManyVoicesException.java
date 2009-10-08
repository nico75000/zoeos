/*
 * TooManyVoicesException.java
 *
 * Created on January 4, 2003, 4:56 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class TooManyVoicesException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>TooManyVoicesException</code> without detail message.
     */
    public TooManyVoicesException() {
    }


    /**
     * Constructs an instance of <code>TooManyVoicesException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TooManyVoicesException(String msg) {
        super(msg);
    }
}
