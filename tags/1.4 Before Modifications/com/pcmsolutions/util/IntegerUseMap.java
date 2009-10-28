package com.pcmsolutions.util;

import com.pcmsolutions.system.IntPool;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 15-Aug-2003
 * Time: 17:00:13
 * To change this template use Options | File Templates.
 */
public class IntegerUseMap implements Serializable {
    static final long serialVersionUID = 1;

    protected final TreeMap<Integer,Integer> useMap = new TreeMap<Integer,Integer>();

    public void addIntegerReference(Integer i) {
        addIntegerReference(i, 1);
    }

    public void removeIntegerReference(Integer i) {
        useMap.remove(i);
    }

    public void addIntegerReferences(Integer[] refs) {
        for (int i = 0; i < refs.length; i++)
            addIntegerReference(refs[i], 1);
    }

    public void addIntegerReference(Integer i, int numRefs) {
        if (i == null)
            return;
        Integer count = useMap.get(i);
        if (count == null)
            useMap.put(i, IntPool.get(numRefs));
        else
            useMap.put(i, IntPool.get(count.intValue() + numRefs));
    }

    public int size() {
        return useMap.size();
    }

    public void clear() {
        useMap.clear();
    }

    public Integer[] getIntegers() {
        return (Integer[]) getUsedIntegerSet().toArray(new Integer[size()]);
    }

    public Set getUsedIntegerSet() {
        return ((TreeMap)useMap.clone()).keySet();
    }

    public boolean containsAnyOf(Integer[] ints) {
        for (int i = 0; i < ints.length; i++)
            if (getCountForInteger(ints[i]) > 0)
                return true;
        return false;
    }

    public int getCountForInteger(Integer i) {
        Integer c = (Integer) useMap.get(i);
        if (c == null)
            return 0;
        return c.intValue();
    }

    public Map<Integer,Integer> getUseMap() {
        return (TreeMap<Integer,Integer>) useMap.clone();
    }

    public IntegerUseMap mergeUseMap(IntegerUseMap mergingUseMap) {
        Map.Entry<Integer,Integer> me;
        for (Iterator <Map.Entry<Integer,Integer>> i = mergingUseMap.getUseMap().entrySet().iterator(); i.hasNext();) {
            me = i.next();
            addIntegerReference(me.getKey(), me.getValue().intValue());
        }
        return this;
    }
}
