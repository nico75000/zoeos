/*
 * DeviceException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.database;


/**
 * @author pmeehan
 */
public class EmptyException extends Exception {
    Integer index;

    public EmptyException() {
        super("Empty");
    }

    public EmptyException(Integer dbi) {
        this.index = dbi;
    }

    public Integer getIndex() {
        return index;
    }
}
