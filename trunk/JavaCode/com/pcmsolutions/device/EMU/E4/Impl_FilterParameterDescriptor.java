package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.FilterParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.IntPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 23:51:05
 */
class Impl_FilterParameterDescriptor extends Impl_GeneralParameterDescriptor implements FilterParameterDescriptor {
    private Integer filterType;

    public Impl_FilterParameterDescriptor() {
    }

    public void init(Integer id, MinMaxDefault mmd, int loc) {
        init(id, mmd, loc, IntPool.get(0)); // defaults to 0 ( 2 pole lowpass)
    }

    public void init(Integer id, MinMaxDefault mmd, int loc, Integer filterType) {
        this.filterType = filterType;
        super.init(id, mmd, loc);
        units = (String) ((Map) ParameterTables.filterParam_u.get(filterType)).get(id);
        presentationString = (String) ((Map) ParameterTables.filterParam_ps.get(filterType)).get(id);
    }

    protected void makeStr4ValMap() {
        Map m = (Map) ParameterTables.filterValue_ps.get(filterType);
        String[] valStrs = (String[]) m.get(id);
        if (valStrs == null) {
            // id not in use for this filter type
            stringForValueMap = null;
            return;
        }

        stringForValueMap = ParameterTables.createStringForValueMap();

        int max = mmd.getMax().intValue();
        int min = mmd.getMin().intValue();

        if (valStrs.length <= max - min)
            throw new IllegalArgumentException("Problem with parameter database?");

        for (int n = 0; n <= max - min; n++)
            stringForValueMap.put(IntPool.get(n + min), valStrs[n]);
    }

    protected void makeVal4StrMap() {
        if (stringForValueMap == null) {
            valueForStringMap = null;
            return;
        }
        super.makeVal4StrMap();
    }

    public List getStringForValueList() {
        if (stringForValueMap == null)
            return new ArrayList();

        return super.getStringForValueList();
    }

    public List getUnitlessStringForValueList() {
        if (stringForValueMap == null)
            return new ArrayList();
        return super.getUnitlessStringForValueList();
    }

    public Integer getValueForString(String valueString) throws ParameterValueOutOfRangeException {
        if (valueForStringMap == null)
            return IntPool.get(0);
        return super.getValueForString(valueString);
    }

    public Integer getValueForUnitlessString(String valueUnitlessString) throws ParameterValueOutOfRangeException {
        if (valueForStringMap == null)
            return IntPool.get(0);
        return super.getValueForUnitlessString(valueUnitlessString);
    }

    public String getStringForValue(Integer value) throws ParameterValueOutOfRangeException {
        if (stringForValueMap == null)
            return "";
        return super.getStringForValue(value);
    }

    // returns units appended to string value
    public String getUnitlessStringForValue(Integer value) throws ParameterValueOutOfRangeException {
        if (stringForValueMap == null)
            return "";
        return super.getUnitlessStringForValue(value);
    }

    public Integer setFilterType(Integer filterType) {
        int ftv = filterType.intValue();

        // if (filterType.intValue() == 5)
        //   System.out.println("test");

        if (ftv < 0 || ftv > ParameterTables.numFilterTypes - 1)
            filterType = IntPool.get(0);


        units = (String) ((Map) ParameterTables.filterParam_u.get(filterType)).get(id);
        presentationString = (String) ((Map) ParameterTables.filterParam_ps.get(filterType)).get(id);
        this.filterType = filterType;

        stringForValueMap = null;
        valueForStringMap = null;
        makeMaps();

        return this.filterType;
    }

    public boolean isCurrentlyActive() {
        if (stringForValueMap != null)
            return true;
        return false;
    }

    public FilterParameterDescriptor duplicate() {
        Impl_FilterParameterDescriptor pd = new Impl_FilterParameterDescriptor();
        pd.init(id, mmd, hpos, filterType);
        return pd;
    }
}
