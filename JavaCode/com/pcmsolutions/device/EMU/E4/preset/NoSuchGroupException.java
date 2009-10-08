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
public class NoSuchGroupException extends Exception {

    /**
     * Creates a new instance of <code>NoSuchVoiceException</code> without detail message.
     */
    public NoSuchGroupException() {
    }


    /**
     * Constructs an instance of <code>NoSuchVoiceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchGroupException(String msg) {
        super(msg);
    }
}
