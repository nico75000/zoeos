/*
 * NoSuchLinkException.java
 *
 * Created on January 4, 2003, 4:56 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class NoSuchLinkException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>NoSuchLinkException</code> without detail message.
     */
    public NoSuchLinkException() {
    }


    /**
     * Constructs an instance of <code>NoSuchLinkException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchLinkException(String msg) {
        super(msg);
    }
}
