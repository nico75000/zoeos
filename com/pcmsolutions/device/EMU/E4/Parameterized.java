package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedParameters;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;

import java.io.Serializable;
import java.util.*;

abstract class Parameterized implements Serializable, ZDisposable, IsolatedParameters {
    protected Hashtable params = new Hashtable();
    protected boolean defaultConversionValues = false;
    protected ParameterContext parameterContext;

    // new constructor with option to default values from given ParameterContext
    public Parameterized(ParameterContext pc, boolean defValues) {
        this.parameterContext = pc;
        if (defValues)
            params.putAll(pc.getIdsAndDefaultsAsMap());
    }

    // copy constructor
    protected Parameterized(Parameterized src) {
        this(src, src.parameterContext);
    }

    // copy constructor from IsolatedParameters
    protected Parameterized(IsolatedParameters src, ParameterContext pc) {
        params.putAll(src.getIdValMap());
        translateToParameterContext(pc);
    }

    // copy constructor with translation to given ParameterContext
    protected Parameterized(Parameterized src, ParameterContext pc) {
        params.putAll(src.params);
        if (src.parameterContext != pc)
            translateToParameterContext(pc);
        else
            this.parameterContext = src.parameterContext;
    }

    // constructor for a new Parameterized with given initial values
    protected Parameterized(ParameterContext pc, Integer[] idValues) {
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


        /*TreeMap p2 = new TreeMap(params);
        p2.keySet().removeAll(Arrays.asList(ids));
        Collection c = p2.values();
        return (Integer[]) c.toArray(new Integer[c.size()]);
        */
    }

    public Integer getValue(Integer id) throws IllegalParameterIdException {
        return getValues(new Integer[]{id})[0];
    }

    public boolean containsId(Integer id) {
        return params.containsKey(id);
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

    public Integer[] getValues(Integer[] ids) throws IllegalParameterIdException {
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

    public void defaultValues(Integer[] ids) throws IllegalParameterIdException {
        for (int i = 0; i < ids.length; i++)
            try {
                setValues(new Integer[]{ids[i]}, new Integer[]{parameterContext.getParameterDescriptor(ids[i]).getDefaultValue()});
            } catch (ParameterValueOutOfRangeException e) {
                // should never get here!!
            }
    }

    public void setValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        Integer[] copyVals = (Integer[]) values.clone();
        int len = ids.length;
        if (len != copyVals.length)
            throw new IllegalArgumentException(this.getClass().toString() + ":setValues -> num ids,num values mismatch!");

        for (int n = 0; n < len; n++) {
            if (!parameterContext.paramExists(ids[n]))
                throw new IllegalParameterIdException();
            copyVals[n] = checkValue(ids[n], copyVals[n]);
        }

        for (int n = 0; n < len; n++)
            params.put(ids[n], copyVals[n]);
    }

    public void offsetValues(Integer[] ids, Integer[] offsettingValues) throws IllegalParameterIdException {
        try {
            offsetValues(ids, offsettingValues, true);
        } catch (ParameterValueOutOfRangeException e) {
            // should never get here
        }
    }

    public void offsetValues(Integer[] ids, Integer[] offsettingValues, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        Integer[] copyVals = (Integer[]) offsettingValues.clone();
        int len = ids.length;
        if (len != copyVals.length)
            throw new IllegalArgumentException(this.getClass().toString() + ":offsetValues -> num ids,num offsettingValues mismatch!");

        for (int n = 0; n < len; n++) {
            if (!parameterContext.paramExists(ids[n]))
                throw new IllegalParameterIdException();

            if (constrain)
                copyVals[n] = parameterContext.getParameterDescriptor(ids[n]).constrainValue(IntPool.get(copyVals[n].intValue() + getValue(ids[n]).intValue()));
            else
                copyVals[n] = checkValue(ids[n], IntPool.get(copyVals[n].intValue() + getValue(ids[n]).intValue()));
        }

        for (int n = 0; n < len; n++)
            params.put(ids[n], copyVals[n]);
    }

    protected Integer checkValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        if (!parameterContext.getParameterDescriptor(id).isValidValue(value))
            throw new ParameterValueOutOfRangeException();
        return value;
    }

    /*public void setValues(Integer[] idVals) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        if (idVals.elementCount % 2 != 0)
            throw new IllegalArgumentException(this.getClass().toString() + ":setValues -> num ids,num values mismatch!");
        for (int i = 0, j = idVals.elementCount; i < j; i += 2) {
            if (!presetContext.paramExists(idVals[i]))
                throw new IllegalParameterIdException();
            if (!presetContext.getParameterDescriptor(idVals[i]).isValidValue(idVals[i + 1]))
                throw new ParameterValueOutOfRangeException();
        }

        for (int i = 0, j = idVals.elementCount; i < j; i += 2) {
            params.put(idVals[i], idVals[i + 1]);
        }
    } */

    protected void defaultValues() {
        params.clear();
        params.putAll(parameterContext.getIdsAndDefaultsAsMap());
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
                    continue;
                    // should never get here (TODO!! it)
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

        //params.clear();

        Integer id;
        GeneralParameterDescriptor pd;
        for (int n = 0; n < len; n += 2) {
            id = idValues[n];
            try {
                pd = parameterContext.getParameterDescriptor(id);
            } catch (IllegalParameterIdException e) {
                continue;
            }
            // if ( id.intValue() == 236)
            //  System.out.print("236");
            params.put(id, constrainValue(pd, idValues[n + 1]));
        }
    }

    public void zDispose() {
    }
}

