package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedParameters;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Jan-2004
 * Time: 16:07:25
 * To change this template use Options | File Templates.
 */
class Impl_SerializableIsolatedParameters implements IsolatedParameters, Serializable {
    static final long serialVersionUID = 1;

    private TreeMap params;

    public Impl_SerializableIsolatedParameters(Map params) {
        this.params = new TreeMap(params);
    }

    public Integer getValue(Integer id) throws IllegalParameterIdException {
        return getValues(new Integer[]{id})[0];
    }

    public boolean containsId(Integer id) {
        return params.containsKey(id);
    }

    public Map getIdValMap() {
        return (Map) params.clone();
    }

    // returns sorted array
    public Integer[] getAllIds() {
        Collection c = new TreeMap(params).keySet();
        return (Integer[]) c.toArray(new Integer[c.size()]);
    }

    // returns sorted array
    public Integer[] getAllIdsExcept(Integer[] ids) {
        Collection c = new TreeMap(params).keySet();
        c.removeAll(Arrays.asList(ids));
        return (Integer[]) c.toArray(new Integer[c.size()]);
    }

    public Integer[] getIdValues() {
        Integer[] vals = getAllValues();
        Integer[] ids = getAllIds();
        Integer[] idVals = new Integer[vals.length + ids.length];
        for (int i = 0,j = ids.length * 2; i < j; i += 2) {
            idVals[i] = ids[i / 2];
            idVals[i + 1] = vals[i / 2];
        }
        return idVals;
    }

    // returns sorted array
    public Integer[] getAllValues() {
        Collection c = new TreeMap(params).values();
        return (Integer[]) c.toArray(new Integer[c.size()]);
    }

    // returns sorted array
    public Integer[] getAllValuesExcept(Integer[] ids) {
        TreeMap p2 = new TreeMap(params);
        for (int i = 0; i < ids.length; i++)
            p2.remove(ids[i]);
        Collection c = p2.values();
        return (Integer[]) c.toArray(new Integer[c.size()]);
    }

    public Integer[] getValues(Integer[] ids) throws IllegalParameterIdException {
        int len = ids.length;
        Integer[] retVals = new Integer[len];
        Integer v;
        Integer id;
        for (int n = 0; n < len; n++) {
            id = ids[n];
            v = (Integer) params.get(id);
            if (v == null)
                throw new IllegalParameterIdException();

            retVals[n] = v;
        }
        return retVals;
    }

    public void zDispose() {
    }
}
