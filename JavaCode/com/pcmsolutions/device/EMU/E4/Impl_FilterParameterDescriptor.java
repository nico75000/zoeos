package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.FilterParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.NoteUtilities;

import java.util.Map;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 23:51:05
 */
class Impl_FilterParameterDescriptor extends AbstractParameterDescriptor implements FilterParameterDescriptor {
    private Integer filterType;
    protected Map<String, Integer> valueForStringMap;

    public Impl_FilterParameterDescriptor() {
    }

    public void init(Integer id, MinMaxDefault mmd, int loc) {
        init(id, mmd, loc, IntPool.get(0)); // defaults to 0 ( 2 pole lowpass)
    }

    public void init(Integer id, MinMaxDefault mmd, int loc, Integer filterType) {
        this.filterType = filterType;
        super.init(id, mmd, loc);
        makeVal4StrMap();
    }

    public String getPresentationString() {
        return (String) ((Map) ParameterTables.filterParam_ps.get(filterType)).get(IntPool.get(id));
    }

    public String getUnits() {
        return (String) ((Map) ParameterTables.filterParam_u.get(filterType)).get(IntPool.get(id));
    }

    protected void makeVal4StrMap() {
        String[] valStrs = getValueStrings();
        if (valStrs == null) {
            valueForStringMap = null;
            return;
        }
        valueForStringMap = ParameterTables.createValueForStringMap();
        int min = mmd.getMin().intValue();
        for (int i = 0, j = valStrs.length; i < j; i++)
            valueForStringMap.put(valStrs[i], IntPool.get(i + min));
    }

    protected String[] getValueStrings() {
        return ParameterTables.filterValue_ps[filterType.intValue()][id - ParameterTables.baseFilterSubId];
    }

    protected Map<String, Integer> getString2ValueMap() {
        return valueForStringMap;
    }

    public Integer getValueForUnitlessString(String valueUnitlessString) throws ParameterValueOutOfRangeException {
        try {
            return super.getValueForUnitlessString(valueUnitlessString);
        } catch (ParameterValueOutOfRangeException e) {
            // try for note name
            int n = NoteUtilities.Note.getValueForString(valueUnitlessString);
            if (n != -1) {
                double freq = NoteUtilities.getFreqForNote(n);
                return getValueForUnitlessString(String.valueOf(freq));
            }
            throw e;
        }
    }

    boolean isNumeric() {
        return ParameterTables.filterValue_ps_numerics[filterType.intValue()][id - ParameterTables.baseFilterSubId] != null;
    }

    double[] getNumericValueStrings() {
        return ParameterTables.filterValue_ps_numerics[filterType.intValue()][id - ParameterTables.baseFilterSubId];
    }

    public Integer setFilterType(Integer filterType) {
        int ftv = filterType.intValue();

        if (ftv < 0 || ftv > ParameterTables.numFilterTypes - 1)
            filterType = IntPool.get(0);

        this.filterType = filterType;
        makeVal4StrMap();
        return this.filterType;
    }

    public boolean isCurrentlyActive() {
        return getValueStrings() != null;
    }

    public FilterParameterDescriptor duplicate() {
        Impl_FilterParameterDescriptor pd = new Impl_FilterParameterDescriptor();
        pd.init(getId(), mmd, hpos, filterType);
        return pd;
    }

    protected String[] getValueTips() {
        return getValueStrings();
    }
}
