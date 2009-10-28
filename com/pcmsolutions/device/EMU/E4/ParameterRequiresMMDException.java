/*
 * NoSuchLinkException.java
 *
 * Created on January 4, 2003, 4:56 PM
 */

package com.pcmsolutions.device.EMU.E4;

/**
 *
 * @author  pmeehan
 */
class ParameterRequiresMMDException extends Exception {
    private Integer id;

    public ParameterRequiresMMDException(Integer id, String msg) {
        super(msg);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
