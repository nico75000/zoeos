package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;

/**
 * User: paulmeehan
 * Date: 24-Mar-2004
 * Time: 12:29:42
 */
public interface DatabaseZone  extends DatabaseParameterized{
    Integer getZone();

    Integer getSample();

    Integer getPreset();

    Integer getVoice();

    void setValue(Integer id, Integer val) throws IllegalParameterIdException, ParameterValueOutOfRangeException;
}
