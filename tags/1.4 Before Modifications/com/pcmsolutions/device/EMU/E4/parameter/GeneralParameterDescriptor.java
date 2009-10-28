package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.device.EMU.E4.MinMaxDefault;
import com.pcmsolutions.gui.IconAndTipCarrier;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

public interface GeneralParameterDescriptor extends IconAndTipCarrier, Serializable {
    public MinMaxDefault getMMD();

    public Integer getId();

    public boolean isValidValue(Integer value);

    public String getPresentationString();

    public String getReferenceString();

    public String getUnits();

    public Integer getMinValue();

    public Integer getMaxValue();

    public Integer getDefaultValue();

    public Integer constrainValue(Integer value);

    public String getStringForValue(Integer value) throws ParameterValueOutOfRangeException;

    // returns units appended to string value
    public String getUnitlessStringForValue(Integer value) throws ParameterValueOutOfRangeException;

    public Integer getValueForString(String valueString) throws ParameterValueOutOfRangeException;

    public Integer getValueForUnitlessString(String valueExString) throws ParameterValueOutOfRangeException;

    public Integer getNextValue(Integer v); // usually +1 ( cords are different because they have a discontinuous space )

    public Integer getPreviousValue(Integer v); // usually -1 ( cords are different because they have a discontinuous space )

    public int getHierarchicalPosition();

    public String getCategory();

    public java.util.List<String> getStringList();

    public java.util.List<String> getUnitlessStringList();

    public String getTipForValue(Integer value) throws ParameterValueOutOfRangeException;

    public boolean shouldUseSpinner();

}

