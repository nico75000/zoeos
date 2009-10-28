package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.DeviceException;

/**
 * User: paulmeehan
 * Date: 23-Jul-2004
 * Time: 19:47:09
 */
public class NoSuchContextIndexException extends ContextException {
    public NoSuchContextIndexException(Integer index) {
        super("no such context index: " + index);
    }
}