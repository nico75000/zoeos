package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 23:49:00
 */
abstract class AbstractParameterDescriptor implements GeneralParameterDescriptor {
    protected Integer id;
    protected MinMaxDefault mmd;
    protected int hpos;
    protected String presentationString;
    protected String refName;
    protected String units;
    protected Map imageForValueMap;
    protected boolean hasImagesForValues;
    protected String category;
    protected String collaboration;
    protected boolean useSpinner;

    public AbstractParameterDescriptor() {
    }

    public void init(Integer id, MinMaxDefault mmd, int loc) {
        this.id = id;                                   // guaranteed non-null
        this.mmd = mmd;                                 // guaranteed non-null
        this.hpos = loc;
        presentationString = (String) ParameterTables.id2ps.get(id);    // guaranteed non-null
        refName = (String) ParameterTables.id2rs.get(id);               // guaranteed non-null
        units = (String) ParameterTables.id2us.get(id);                 // possibly null
        imageForValueMap = (Map) ParameterTables.id2v2i.get(id);        // possibly null
        category = (String) ParameterTables.id2cs.get(id);              // possibly null
        collaboration = (String) ParameterTables.id2co.get(id);         // possibly null
        this.useSpinner = ((Boolean) ParameterTables.id2useSpinner.get(id)).booleanValue();// guaranteed non-null
    }

    abstract protected void makeMaps();

    public boolean equals(Object obj) {
        if (obj instanceof GeneralParameterDescriptor) {
            GeneralParameterDescriptor pd = (GeneralParameterDescriptor) obj;
            if (pd.getId().equals(id) && pd.getMMD().equals(mmd))
                return true;
        } else if (obj instanceof Integer)
            return obj.equals(id);
        return false;
    }

    public String toString() {
        //return category + "    " +  presentationString;
        return refName;
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

    protected int assertValue(final Integer value) throws ParameterValueOutOfRangeException {
/*            if (!isValidValue(value))
                if (id.intValue() == 250) {      // multimode submix
                    final Integer id = mmd.getID();
                    final Integer min = (value.intValue() < mmd.getMin().intValue() ? value : mmd.getMin());
                    final Integer max = (value.intValue() > mmd.getMax().intValue() ? value : mmd.getMax());
                    final Integer def = mmd.getDefault();
                    mmd = new MinMaxDefault() {
                        public Integer getID() {
                            return id;
                        }

                        public Integer getMin() {
                            return min;
                        }

                        public Integer getMax() {
                            return max;
                        }

                        public Integer getDefault() {
                            return def;
                        }
                    };
                    makeMaps();
                } else
                    throw new ParameterValueOutOfRangeException();
  */
        if (!isValidValue(value))
            throw new ParameterValueOutOfRangeException();

        return value.intValue();
    }

    public Integer getDefaultValue() {
        return mmd.getDefault();
    }

    public String getPresentationString() {
        return presentationString;
    }

    public String getCategory() {
        return category;
    }

    public String getCollaboration() {
        return collaboration;
    }

    public MinMaxDefault getMMD() {
        return mmd;
    }

    public Integer getId() {
        return id;
    }

    public Integer getMaxValue() {
        return mmd.getMax();
    }

    public Integer getMinValue() {
        return mmd.getMin();
    }

    public String getReferenceString() {
        return refName;
    }

    protected abstract Map getStringForValueMap();

    protected abstract Map getValueForStringMap();

    public String getStringForValue(Integer value) throws ParameterValueOutOfRangeException {
        assertValue(value);
        if (units != null)
            return (String) getStringForValueMap().get(value) + units;
        return (String) getStringForValueMap().get(value);
    }

    // returns units appended to string value
    public String getUnitlessStringForValue(Integer value) throws ParameterValueOutOfRangeException {
        assertValue(value);
        return (String) getStringForValueMap().get(value);
    }

    public Integer getValueForString(String valueString) throws ParameterValueOutOfRangeException {
        return (Integer) getValueForStringMap().get(stripUnits(valueString));
    }

    public Integer getValueForUnitlessString(String valueUnitlessString) throws ParameterValueOutOfRangeException {
        return (Integer) getValueForStringMap().get(valueUnitlessString);
    }

    private String stripUnits(String valueString) {
        if (units != null)
            return valueString.substring(0, valueString.indexOf(units));

        return valueString;
    }

    public int getHierarchicalPosition() {
        return hpos;
    }

    public String getUnits() {
        return units;
    }

    public boolean hasImagesForValues() {
        if (imageForValueMap != null && imageForValueMap.size() > 0)
            return true;
        return false;
    }

    public Map getImageForValueMap() {
        return imageForValueMap;
    }

    public boolean shouldUseSpinner() {
        return useSpinner;
    }

    public boolean isValidValue(Integer value) {
        if (value != null) {
            int v = value.intValue();
            if (v <= mmd.getMax().intValue() && v >= mmd.getMin().intValue())
                return true;
        }
        return false;
    }

    public Image getImageForValue(Integer value) throws ParameterValueOutOfRangeException {
        assertValue(value);
        if (imageForValueMap != null)
            return (Image) imageForValueMap.get(value);
        else
            return null;
    }

    public Icon getIcon() {
        return null;
    }

    public String getToolTipText() {
        return category + " " + presentationString;
    }
}
