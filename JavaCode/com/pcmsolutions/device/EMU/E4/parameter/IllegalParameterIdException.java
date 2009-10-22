/*
 * IllegalParameterId.java
 *
 * Created on January 2, 2003, 7:41 PM
 */

package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.system.IntPool;

/**
 *
 * @author  pmeehan
 */
public class IllegalParameterIdException extends ParameterException {
    public IllegalParameterIdException(Integer id) {
        super("illegal parameter id", id);
    }
    public IllegalParameterIdException() {
        super("illegal parameter id", IntPool.minus_one);
    }
}
