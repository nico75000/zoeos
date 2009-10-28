/*
 * IllegalParameterId.java
 *
 * Created on January 2, 2003, 7:41 PM
 */

package com.pcmsolutions.system;

/**
 *
 * @author  pmeehan
 */
public class CommandFailedException extends Exception {

    /**
     * Creates a new instance of <code>IllegalParameterId</code> without detail message.
     */
    public CommandFailedException() {
    }


    /**
     * Constructs an instance of <code>IllegalParameterId</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CommandFailedException(String msg) {
        super(msg);
    }
}
