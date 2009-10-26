package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.IntPool;

import java.util.*;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 23:51:34
 */
class Impl_GeneralParameterDescriptor extends AbstractParameterDescriptor {
    protected Map stringForValueMap;
    protected Map valueForStringMap;
    protected Map tipForValueMap;

    public Impl_GeneralParameterDescriptor() {
    }

    public void init(Integer id, MinMaxDefault mmd, int loc) {
        super.init(id, mmd, loc);
        makeMaps();
    }

    protected Map getStringForValueMap() {
        return stringForValueMap;
    }

    protected Map getValueForStringMap() {
        return valueForStringMap;
    }

    public String getTipForValue(Integer value) throws ParameterValueOutOfRangeException {
        assertValue(value);
        if (tipForValueMap != null)
            return (String) tipForValueMap.get(value);
        else
            return null;
    }

    public List getStringForValueList() {
        if (units == null)
            return getUnitlessStringForValueList();

        int min = mmd.getMin().intValue();
        int max = mmd.getMax().intValue();
        ArrayList l = new ArrayList();
        l.ensureCapacity(max - min + 1);
        for (int i = min; i <= max; i++)
            l.add(stringForValueMap.get(IntPool.get(i)) + units);
        return l;
    }

    public List getUnitlessStringForValueList() {
        int min = mmd.getMin().intValue();
        int max = mmd.getMax().intValue();
        ArrayList l = new ArrayList();
        l.ensureCapacity(max - min + 1);
        for (int i = min; i <= max; i++)
            l.add(stringForValueMap.get(IntPool.get(i)));
        return l;
    }

    public Map getTipForValueMap() {
        return tipForValueMap;
    }

    protected final void makeMaps() {
        synchronized (this) {
            makeStr4ValMap();
            makeVal4StrMap();
            tipForValueMap = (Map) ParameterTables.id2v2ts.get(id);         // possibly null
        }
    }

    protected void makeStr4ValMap() {
        int minv = mmd.getMin().intValue();
        int maxv = mmd.getMax().intValue();

        stringForValueMap = (Map) ParameterTables.id2v2ps.get(id);

        if (stringForValueMap == null) {
            stringForValueMap = ParameterTables.defStringForValueMap;
            valueForStringMap = ParameterTables.defValueForStringMap;
            return;
        } else if (stringForValueMap.size() != maxv - minv + 1) {
            // on the remote chance there is a mismatch, just make a new copy and addDesktopElement in some defaults for missing values
            TreeMap new_v2ps = new TreeMap();
            Integer key;
            for (int n = minv; n <= maxv; n++) {
                key = IntPool.get(n);
                if (stringForValueMap.containsKey(key))
                    new_v2ps.put(key, stringForValueMap.get(key));
                else
                    new_v2ps.put(key, Integer.toString(n));
            }
            stringForValueMap = new_v2ps;
        }
    }

    protected void makeVal4StrMap() {
        if (valueForStringMap == null) {
            valueForStringMap = ParameterTables.createValueForStringMap();
            Iterator i = stringForValueMap.entrySet().iterator();
            Map.Entry e;
            while (i.hasNext()) {
                e = (Map.Entry) i.next();
                valueForStringMap.put(e.getValue(), e.getKey());
            }
        }
    }
}
