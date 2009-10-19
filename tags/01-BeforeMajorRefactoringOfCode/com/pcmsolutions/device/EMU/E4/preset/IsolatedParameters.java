package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.system.ZDisposable;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-Aug-2003
 * Time: 14:22:29
 * To change this template use Options | File Templates.
 */
public interface IsolatedParameters extends ZDisposable {
    public Integer[] getAllIds();

    public Integer[] getAllIdsExcept(Integer[] ids);

    public Integer[] getAllValues();

    public Integer[] getAllValuesExcept(Integer[] ids);

    public Integer[] getValues(Integer[] ids) throws IllegalParameterIdException;

    public Integer[] getIdValues();

    public Integer getValue(Integer id) throws IllegalParameterIdException;

    public boolean containsId(Integer id);

    public Map getIdValMap();
}
