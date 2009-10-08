/*
 * ReadOnlySampleException.java
 *
 * Created on January 5, 2003, 2:45 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

/**
 *
 * @author  pmeehan
 */
public class ReadOnlySampleException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>ReadOnlySampleException</code> without detail message.
     */
    public ReadOnlySampleException() {
    }


    /**
     * Constructs an instance of <code>ReadOnlySampleException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ReadOnlySampleException(String msg) {
        super(msg);
    }
}
