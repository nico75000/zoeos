package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;

import java.util.Map;

/**
 * User: paulmeehan
 * Date: 24-Mar-2004
 * Time: 12:39:52
 */
public interface DatabaseParameterized {
    // returns sorted array
    Integer[] getAllValues();

    // returns sorted array
    Integer[] getAllValuesExcept(Integer[] ids);

    Integer getValue(Integer id) throws IllegalParameterIdException;

    boolean containsId(Integer id);

    // returns sorted array
    Integer[] getAllIds();

    // returns sorted array
    Integer[] getAllIdsExcept(Integer[] ids);

    Integer[] getIdValues();

    Integer[] getValues(Integer[] ids) throws IllegalParameterIdException;

    void defaultValues(Integer[] ids) throws IllegalParameterIdException;

    void setValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException;

    void setValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException;

    void offsetValues(Integer[] ids, Integer[] offsettingValues) throws IllegalParameterIdException;

    void offsetValues(Integer[] ids, Integer[] offsettingValues, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException;

    void offsetValue(Integer id, Integer offset, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException;

    Map getIdValMap();
}
