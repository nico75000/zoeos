package com.pcmsolutions.system.preferences;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:47:06
 * To change this template use Options | File Templates.
 */
public class Impl_ZIntPref extends Impl_ZPref implements ZIntPref {
    public Impl_ZIntPref(Preferences prefs, String key, int def, String presentationName, String description) {
        super(prefs, key, String.valueOf(def), presentationName, description);
    }

    public Impl_ZIntPref(Preferences prefs, String key, int def) {
        super(prefs, key, String.valueOf(def));
    }

    public Impl_ZIntPref(Preferences prefs, String key, int def, String presentationName, String description, String category) {
        super(prefs, key, String.valueOf(def).toString(), presentationName, description, category);
    }

    public synchronized void putDefault() {
        getPrefs().putInt(getKey(), Integer.valueOf(getDefaultString()).intValue());
    }

    public void putValueString(String strVal) {
        putValue(Integer.valueOf(strVal));
    }

    public Object getValueObject() {
        return Integer.valueOf(getValueString());
    }

    public synchronized void putValue(Integer i) {
        putValue(i.intValue());
    }

    public synchronized void offsetValue(int off) {
        putValue(getValue() + off);
    }

    public synchronized void putValue(int i) {
        if (i < getMinValue())
            i = getMinValue();
        else if (i > getMaxValue())
            i = getMaxValue();
        getPrefs().putInt(getKey(), i);
    }

    public int getValue() {
        return Integer.valueOf(getValueString()).intValue();
    }

    public int getDefault() {
        return Integer.valueOf(getDefaultString()).intValue();
    }

    public int getMinValue() {
        return Integer.MIN_VALUE;
    }

    public int getMaxValue() {
        return Integer.MAX_VALUE;
    }

    public int getIncrementValue() {
        return 1;
    }
}
