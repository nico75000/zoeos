package com.pcmsolutions.system.preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:31:35
 * To change this template use Options | File Templates.
 */
public interface ZIntPref extends ZPref{
    public void putValue(int i);
    public void putValue(Integer i);
    public void offsetValue(int off);
    public int getValue();
    public int getDefault();
    public int getMinValue();
    public int getMaxValue();
    public int getIncrementValue();

}
