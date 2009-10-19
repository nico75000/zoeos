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
public class ZDeviceException extends Exception {
    protected ZExternalDevice device;

    /**
     * Creates a new instance of <code>IllegalParameterId</code> without detail message.
     */
    public ZDeviceException(ZExternalDevice d, String msg) {
        super(msg);
        this.device = device;
    }

    public ZExternalDevice getDevice() {
        return device;
    }
    /**
     * Constructs an instance of <code>IllegalParameterId</code> with the specified detail message.
     * @param msg the detail message.
     */
}
