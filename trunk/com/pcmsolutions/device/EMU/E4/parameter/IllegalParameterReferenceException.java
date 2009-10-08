/*
 * IllegalParameterId.java
 *
 * Created on January 2, 2003, 7:41 PM
 */

package com.pcmsolutions.device.EMU.E4.parameter;

/**
 *
 * @author  pmeehan
 */
public class IllegalParameterReferenceException extends Exception {

    /**
     * Creates a new instance of <code>IllegalParameterId</code> without detail message.
     */
    public IllegalParameterReferenceException() {
    }


    /**
     * Constructs an instance of <code>IllegalParameterId</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IllegalParameterReferenceException(String msg) {
        super(msg);
    }
}
