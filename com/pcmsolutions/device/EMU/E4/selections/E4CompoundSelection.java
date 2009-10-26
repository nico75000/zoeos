package com.pcmsolutions.device.EMU.E4.selections;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 09:19:03
 * To change this template use Options | File Templates.
 */
public interface E4CompoundSelection extends E4Selection {

    public boolean containsClass(Class c);

    // ordered
    public Set getEntrySet();
}
