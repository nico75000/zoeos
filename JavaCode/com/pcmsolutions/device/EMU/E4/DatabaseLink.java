package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;

/**
 * User: paulmeehan
 * Date: 24-Mar-2004
 * Time: 12:29:04
 */
public interface DatabaseLink  extends DatabaseParameterized{
    Integer getLink();

    void setValue(Integer id, Integer val) throws IllegalParameterIdException, ParameterValueOutOfRangeException;

    Integer getPreset();
}
