package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 23:49:00
 */
abstract class AbstractParameterDescriptor implements GeneralParameterDescriptor {
    protected int id;
    protected MinMaxDefault mmd;
    protected int hpos;

    public AbstractParameterDescriptor() {
    }

    public void init(Integer id, MinMaxDefault mmd, int loc) {
        this.id = id.intValue();                        // guaranteed non-null
        this.mmd = mmd;                                 // guaranteed non-null
        this.hpos = loc;
    }

    public boolean equals(Object obj) {
        if (obj instanceof GeneralParameterDescriptor) {
            GeneralParameterDescriptor pd = (GeneralParameterDescriptor) obj;
            if (pd.getId().equals(getId()) && pd.getMMD().equals(mmd))
                return true;
        } else if (obj instanceof Integer)
            return obj.equals(getId());
        return false;
    }

    public String toString() {
        //return category + "    " +  presentationString;
        return ParameterTables.id2rs[id];
    }

    public Integer getNextValue(Integer v) {
        if (isValidValue(v) && !v.equals(mmd.getMax()))
            return IntPool.get(v.intValue() + 1);
        return null;
    }

    public Integer getPreviousValue(Integer v) {
        if (isValidValue(v) && !v.equals(mmd.getMin()))
            return IntPool.get(v.intValue() - 1);
        return null;
    }

    public Integer constrainValue(Integer value) {
        int idv = value.intValue();
        if (idv > mmd.getMax().intValue())
            return mmd.getMax();
        if (idv < mmd.getMin().intValue())
            return mmd.getMin();
        return value;
    }

    protected final int normv(Integer val) {
        return val.intValue() - mmd.getMin().intValue();
    }

    public String getTipForValue(Integer value) throws ParameterValueOutOfRangeException {
        assertValue(value);
        return getValueTips()[normv(value)];
    }

    public List<String> getStringList() {
        if (getUnits() == null)
            return getUnitlessStringList();

        String[] valueStrings = getValueStrings();
        String[] outValueStrings = new String[valueStrings.length];

        for (int i = 0, j = valueStrings.length; i < j; i++)
            outValueStrings[i] = valueStrings[i] + getUnits();

        return Arrays.asList(outValueStrings);
    }

    public List<String> getUnitlessStringList() {
        return Arrays.asList((String[]) getValueStrings().clone());
    }

    protected String[] getValueStrings() {
        return ParameterTables.id2valueStrings[id];  // guaranteed non-null
    }

    protected String[] getValueTips() {
        return ParameterTables.id2tipStrings[id];    // guaranteed non-null
    }

    protected Map<String, Integer> getString2ValueMap() {
        return ParameterTables.id2string2Value.get(id);
    }

    protected int assertValue(final Integer value) throws ParameterValueOutOfRangeException {
        if (!isValidValue(value))
            throw new ParameterValueOutOfRangeException(IntPool.get(id));
        return value.intValue();
    }

    public Integer getDefaultValue() {
        return mmd.getDefault();
    }

    public String getPresentationString() {
        return ParameterTables.id2ps[id];
    }

    public String getCategory() {
        return ParameterTables.id2cs[id];
    }

    public MinMaxDefault getMMD() {
        return mmd;
    }

    public Integer getId() {
        return IntPool.get(id);
    }

    public Integer getMaxValue() {
        return mmd.getMax();
    }

    public Integer getMinValue() {
        return mmd.getMin();
    }

    public String getReferenceString() {
        return ParameterTables.id2rs[id];
    }

    public String getStringForValue(Integer value) throws ParameterValueOutOfRangeException {
        assertValue(value);
        if (getUnits() != null)
            return getValueStrings()[normv(value)] + getUnits();
        return getValueStrings()[normv(value)];
    }

    // returns units appended to string value
    public String getUnitlessStringForValue(Integer value) throws ParameterValueOutOfRangeException {
        assertValue(value);
        return getValueStrings()[normv(value)];
    }

    public Integer getValueForString(String valueString) throws ParameterValueOutOfRangeException {
        return getValueForUnitlessString((stripUnits(valueString)));
    }

    public Integer getValueForUnitlessString(String valueUnitlessString) throws ParameterValueOutOfRangeException {
        Integer v = getString2ValueMap().get(valueUnitlessString);
        if (v == null) {
            if (isNumeric()) {
                double val;
                try {
                    val = Double.parseDouble(valueUnitlessString);
                    return IntPool.get(mmd.getMin().intValue() + ZUtilities.getNearestDoubleIndex(getNumericValueStrings(), val));
                } catch (NumberFormatException e1) {
                }
            }
            throw new ParameterValueOutOfRangeException(IntPool.get(id));
        } else
            return v;
    }

    boolean isNumeric() {
        return ParameterTables.id2valueStringNumerics[id] != null;
    }

    double[] getNumericValueStrings() {
        return ParameterTables.id2valueStringNumerics[id];
    }

    protected final String stripUnits(String valueString) {
        if (getUnits() != null) {
            int index = valueString.trim().toLowerCase().indexOf(getUnits().trim().toLowerCase());
            if (index != -1)
                return valueString.substring(0, index).trim();
        }
        return valueString.trim();
    }

    public int getHierarchicalPosition() {
        return hpos;
    }

    public String getUnits() {
        return ParameterTables.id2us[id];
    }

    public boolean shouldUseSpinner() {
        return ParameterTables.id2useSpinner[id];
    }

    public boolean isValidValue(Integer value) {
        if (value != null) {
            int v = value.intValue();
            if (v <= mmd.getMax().intValue() && v >= mmd.getMin().intValue())
                return true;
        }
        return false;
    }

    public Icon getIcon() {
        return null;
    }

    public String getToolTipText() {
        return ParameterTables.id2cs[id] + " " + ParameterTables.id2ps[id];
    }
}
