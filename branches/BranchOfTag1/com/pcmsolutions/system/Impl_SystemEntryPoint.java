package com.pcmsolutions.system;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 20:30:05
 */
public class Impl_SystemEntryPoint implements SystemEntryPoint {
    private Class coe;
    private String ioe;

    public Impl_SystemEntryPoint(Class coe, String ioe) {
        this.coe = coe;
        this.ioe = ioe;
    }

    public Class getClassOfEntry() {
        return coe;
    }

    public String getInstanceOfEntry() {
        return ioe;
    }

    public String toString() {
        return getClassOfEntry() + ZUtilities.STRING_FIELD_SEPERATOR + getInstanceOfEntry();
    }

    public boolean equals(Object obj) {
        return obj.toString().equals(toString());
    }

    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }
}
