/*
 * NoSuchVoiceException.java
 *
 * Created on January 4, 2003, 4:56 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

/**
 *
 * @author  pmeehan
 */
public class NoSuchVoiceException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>NoSuchVoiceException</code> without detail message.
     */
    public NoSuchVoiceException() {
    }


    /**
     * Constructs an instance of <code>NoSuchVoiceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchVoiceException(String msg) {
        super(msg);
    }
}
