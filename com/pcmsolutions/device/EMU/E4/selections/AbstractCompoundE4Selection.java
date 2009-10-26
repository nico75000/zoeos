package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;

import java.awt.datatransfer.DataFlavor;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 09:06:09
 * To change this template use Options | File Templates.
 */
public class AbstractCompoundE4Selection extends AbstractE4Selection implements E4CompoundSelection {
    protected final TreeMap elements = new TreeMap(); // Integer -> Object

    public AbstractCompoundE4Selection(DeviceContext d) {
        super(d);
    }

    public void addElement(Integer index, E4Selection elem) {
        elements.put(index, elem);


        DataFlavor df;
    }

    public boolean containsClass(Class c) {
        return doContainsClass(c, elements.entrySet(), 1);
    }

    public boolean containsClassDeep(Class c) {
        return doContainsClass(c, elements.entrySet(), -1);
    }

    private boolean doContainsClass(Class c, Set entries, int levels) {
        Map.Entry e;
        for (Iterator i = entries.iterator(); i.hasNext();) {
            e = (Map.Entry) i.next();
            if (e.getValue().getClass().equals(c))
                return true;
            else if (e.getValue() instanceof E4CompoundSelection && levels != 0) {
                doContainsClass(c, ((E4CompoundSelection) e.getValue()).getEntrySet(), levels - 1);
            }
        }
        return false;
    }

    // ordered
    public Set getEntrySet() {
        return ((TreeMap) elements.clone()).entrySet();
    }
}
