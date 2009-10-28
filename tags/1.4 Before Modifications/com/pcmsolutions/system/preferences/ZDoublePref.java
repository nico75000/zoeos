package com.pcmsolutions.system.preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:31:35
 * To change this template use Options | File Templates.
 */
public interface ZDoublePref extends ZPref {
    public void putValue(double d);

    public void putValue(Double d);

    public void offsetValue(double off);

    public double getValue();

    public double getDefault();
}
