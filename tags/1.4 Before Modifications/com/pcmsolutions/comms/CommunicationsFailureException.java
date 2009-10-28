/*
 * CommunicationsFailureException.java
 *
 * Created on January 5, 2003, 2:24 AM
 */

package com.pcmsolutions.comms;

/**
 *
 * @author  pmeehan
 */
public class CommunicationsFailureException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>CommunicationsFailureException</code> without detail message.
     */
    public CommunicationsFailureException() {
    }


    /**
     * Constructs an instance of <code>CommunicationsFailureException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CommunicationsFailureException(String msg) {
        super(msg);
    }
}
