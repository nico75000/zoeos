/*
 * RemoteDeviceUnavailableException.java
 *
 * Created on October 21, 2002, 8:05 PM
 */

package com.pcmsolutions.comms;

/**
 *
 * @author  pmeehan
 */
public class RemoteDeviceDidNotRespondException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>RemoteDeviceUnavailableException</code> without detail message.
     */

    /**
     * Constructs an instance of <code>RemoteDeviceUnavailableException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RemoteDeviceDidNotRespondException(String msg) {
        super(msg);
    }
}
