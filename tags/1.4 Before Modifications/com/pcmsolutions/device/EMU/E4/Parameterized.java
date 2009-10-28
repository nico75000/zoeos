package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedParameters;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.SystemErrors;
import com.pcmsolutions.system.ZDisposable;

import java.io.Serializable;
import java.util.*;

abstract class Parameterized implements Serializable, ZDisposable, IsolatedParameters, DatabaseParameterized {
    protected Hashtable params = new Hashtable();
    protected boolean defaultConversionValues = false;
    protected ParameterContext parameterContext;

    // new constructor with option to default values from given ParameterContext
    protected void initNew(ParameterContext pc, boolean defValues) {
        this.parameterContext = pc;
        if (defValues)
            params.putAll(pc.getIdsAndDefaultsAsMap());
    }

    // copy constructor
    protected void initCopy(Parameterized src) {
        initCopy(src, src.parameterContext);
    }

    // copy constructor from IsolatedParameters
    protected void initDrop(IsolatedParameters src, ParameterContext pc) {
        params.putAll(src.getIdValMap());
        translateToParameterContext(pc);
    }

    // copy constructor with translation to given ParameterContext
    protected void initCopy(Parameterized src, ParameterContext pc) {
        params.putAll(src.params);
        if (src.parameterContext != pc)
            translateToParameterContext(pc);
        else
            this.parameterContext = src.parameterContext;
    }

    // constructor for a new Parameterized with given initial values
    protected void initNew(ParameterContext pc, Integer[] idValues) {
        this.parameterContext = pc;
        initValues(idValues);
    }

    public ParameterContext getParameterContext() {
        return parameterContext;
    }

    public void setParameterContext(ParameterContext parameterContext) {
        this.parameterContext = parameterContext;
    }

    // returns sorted array
    public final Integer[] getAllValues() {
        Collection c = new TreeMap(params).values();
        return (Integer[]) c.toArray(new Integer[c.size()]);
    }

    // returns sorted array
    public final Integer[] getAllValuesExcept(Integer[] ids) {
        TreeMap p2 = new TreeMap(params);
        for (int i = 0; i < ids.length; i++)
            p2.remove(ids[i]);
        Collection c = p2.values();
        return (Integer[]) c.toArray(new Integer[c.size()]);


        /*TreeMap p2 = new TreeMap(params);
        p2.keySet().removeAll(Arrays.asList(ids));
        Collection c = p2.values();
        return (Integer[]) c.toArray(new Integer[c.size()]);
        */
    }

    public final Integer getValue(Integer id) throws IllegalParameterIdException {
        return getValues(new Integer[]{id})[0];
    }

    public final boolean containsId(Integer id) {
        return params.containsKey(id);
    }

    // returns sorted array
    public final Integer[] getAllIds() {
        Collection c = new TreeMap(params).keySet();
        return (Integer[]) c.toArray(new Integer[c.size()]);
    }

    // returns sorted array
    public final Integer[] getAllIdsExcept(Integer[] ids) {
        Collection c = new TreeMap(params).keySet();
        c.removeAll(Arrays.asList(ids));
        return (Integer[]) c.toArray(new Integer[c.size()]);
    }

    public final Integer[] getIdValues() {
        Integer[] vals = getAllValues();
        Integer[] ids = getAllIds();
        Integer[] idVals = new Integer[vals.length + ids.length];
        for (int i = 0, j = ids.length * 2; i < j; i += 2) {
            idVals[i] = ids[i / 2];
            idVals[i + 1] = vals[i / 2];
        }
        return idVals;
    }

    public final Integer[] getValues(Integer[] ids) throws IllegalParameterIdException {
        int len = ids.length;
        Integer[] retVals = new Integer[len];
        Integer v;
        Integer id;
        for (int n = 0; n < len; n++) {
            id = ids[n];
            v = (Integer) params.get(id);
            if (v == null) {
                GeneralParameterDescriptor pd = parameterContext.getParameterDescriptor(id);
                if (pd == null)
                    throw new IllegalParameterIdException();
                v = pd.getDefaultValue();
            }
            retVals[n] = v;
        }
        return retVals;
    }

    public final void defaultValues(Integer[] ids) throws IllegalParameterIdException {
        for (int i = 0; i < ids.length; i++)
            try {
                Integer def = parameterContext.getParameterDescriptor(ids[i]).getDefaultValue();
                setValue(ids[i], def);
            } catch (ParameterValueOutOfRangeException e) {
                // should never get here!!
            }
    }

    protected final void putValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        if (!parameterContext.paramExists(id))
            throw new IllegalParameterIdException();
        params.put(id, finalizeValue(id, value));
    }

    protected final void putValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        int len = ids.length;
        if (len != values.length)
            throw new IllegalArgumentException(this.getClass().toString() + ":putValues -> num ids,num values mismatch!");
        for (int n = 0; n < len; n++)
            putValue(ids[n], values[n]);
    }

    public final void setValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        int len = ids.length;
        if (len != values.length)
            throw new IllegalArgumentException(this.getClass().toString() + ":setValues -> num ids,num values mismatch!");
        for (int n = 0; n < len; n++)
            setValue(ids[n], values[n]);
    }

    public final void offsetValues(Integer[] ids, Integer[] offsettingValues) throws IllegalParameterIdException {
        try {
            offsetValues(ids, offsettingValues, true);
        } catch (ParameterValueOutOfRangeException e) {
            // should never get here
        }
    }

    public final void offsetValues(Integer[] ids, Integer[] offsettingValues, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        int len = ids.length;
        if (len != offsettingValues.length)
            throw new IllegalArgumentException(this.getClass().toString() + ":offsetValues -> num ids,num offsettingValues mismatch!");
        for (int n = 0; n < len; n++)
            offsetValue(ids[n], offsettingValues[n], constrain);
    }

    public  void offsetValue(Integer id, Integer offset, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        if (constrain)
            setValue(id, parameterContext.getParameterDescriptor(id).constrainValue(IntPool.get(offset.intValue() + getValue(id).intValue())));
        else
            setValue(id , getValue(IntPool.get(id.intValue())) + offset.intValue());
    }

    protected Integer finalizeValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        if (!parameterContext.getParameterDescriptor(id).isValidValue(value))
            throw new ParameterValueOutOfRangeException(id);
        return value;
    }

    // take on a new set of ids, but reuses existing id values where possible
    // if old set contains an id not in new set, that id's values will be lost
    // if the new set contains an id not in the old, that id's value will be defaulted
    protected void translateToParameterContext(ParameterContext pc) {
        this.parameterContext = pc;
        Map oldParams = (Map) params.clone();
        params.clear();
        Integer key;
        GeneralParameterDescriptor pd;
        Map newIds = pc.getIdsAndDefaultsAsMap();     // id(Integer) -> default value(Integer)
        for (Iterator i = newIds.keySet().iterator(); i.hasNext();) {
            key = (Integer) i.next();
            if (oldParams.containsKey(key)) {
                try {
                    pd = pc.getParameterDescriptor(key);
                } catch (IllegalParameterIdException e) {
                    SystemErrors.internal(e);
                    continue;
                }
                params.put(key, constrainValue(pd, (Integer) oldParams.get(key)));
            } else
                params.put(key, newIds.get(key));
        }
    }

    protected Integer constrainValue(GeneralParameterDescriptor pd, Integer val) {
        int min, max, ival;
        min = pd.getMinValue().intValue();
        max = pd.getMaxValue().intValue();
        ival = val.intValue();

        if (defaultConversionValues && (ival < min || ival > max))
            val = pd.getDefaultValue();
        else if (ival < min)
            val = IntPool.get(min);
        else if (ival > max)
            val = IntPool.get(max);

        return val;
    }

    public Map getIdValMap() {
        return (Map) params.clone();
    }

    public void initValues(Integer[] idValues) {
        int len = idValues.length;

        // ensure id/value pairs
        if (len % 2 != 0)
            throw new IllegalArgumentException(this.getClass().toString() + ":initValues -> array not id/value pairs!");

        Integer id;
        GeneralParameterDescriptor pd;
        for (int n = 0; n < len; n += 2) {
            id = idValues[n];
            try {
                pd = parameterContext.getParameterDescriptor(id);
            } catch (IllegalParameterIdException e) {
                continue;
            }
            params.put(id, constrainValue(pd, idValues[n + 1]));
        }
    }

    public void zDispose() {
    }
}

