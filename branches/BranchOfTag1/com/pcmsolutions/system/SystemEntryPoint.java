package com.pcmsolutions.system;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 19-Jan-2004
 * Time: 06:26:33
 */
public interface SystemEntryPoint extends Serializable, Comparable{
    public Class getClassOfEntry();
    public String getInstanceOfEntry();
}
