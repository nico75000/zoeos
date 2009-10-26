/*
 * NoSuchContextException.java
 *
 * Created on January 13, 2003, 9:15 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class NoSuchContextException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>NoSuchContextException</code> without detail message.
     */
    public NoSuchContextException() {
    }


    /**
     * Constructs an instance of <code>NoSuchContextException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchContextException(String msg) {
        super(msg);
    }
}
