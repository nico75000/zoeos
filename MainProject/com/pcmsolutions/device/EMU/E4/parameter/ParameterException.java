package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.IntPool;

/**
 * User: paulmeehan
 * Date: 16-Aug-2004
 * Time: 20:04:42
 */
public class ParameterException extends Exception{
    Integer id = IntPool.minus_one;

    public ParameterException(String message, Integer id) {
        super(message);
        this.id = id;
    }
       public ParameterException(String message) {
        super(message);
    }
    public Integer getId() {
        return id;
    }
}
