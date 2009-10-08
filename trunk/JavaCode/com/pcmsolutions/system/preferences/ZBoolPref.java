package com.pcmsolutions.system.preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:31:35
 * To change this template use Options | File Templates.
 */
public interface ZBoolPref extends ZPref{
    public void putValue(Boolean b);
    public void putValue(boolean b);
    public void toggleValue();
    public boolean getValue();
    public boolean getDefault();
}
