/*
 * NoSuchContextException.java
 *
 * Created on January 13, 2003, 9:15 AM
 */

package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.DeviceException;

/**
 *
 * @author  pmeehan
 */
public class NoSuchContextException extends ContextException {
    public NoSuchContextException() {
        super("no such context");
    }
}
