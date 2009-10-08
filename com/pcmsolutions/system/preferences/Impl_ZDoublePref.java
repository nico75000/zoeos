package com.pcmsolutions.system.preferences;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:47:06
 * To change this template use Options | File Templates.
 */
public class Impl_ZDoublePref extends Impl_ZPref implements ZDoublePref {
    public Impl_ZDoublePref(Preferences prefs, String key, double def, String presentationName, String description) {
        super(prefs, key, String.valueOf(def), presentationName, description);
    }

    public Impl_ZDoublePref(Preferences prefs, String key, double def) {
        super(prefs, key, String.valueOf(def));
    }

    public Impl_ZDoublePref(Preferences prefs, String key, double def, String presentationName, String description, String category) {
        super(prefs, key, String.valueOf(def).toString(), presentationName, description, category);
    }

    public void putDefault() {
        getPrefs().putDouble(getKey(), Double.valueOf(getDefaultString()).doubleValue());
    }

    public void putValueString(String strVal) {
        putValue(Double.valueOf(strVal));
    }

    public Object getValueObject() {
        return Double.valueOf(getValueString());
    }

    public synchronized void putValue(Double d) {
        putValue(d.doubleValue());
    }

    public synchronized void offsetValue(double off) {
        putValue(getValue() + off);
    }

    public synchronized void putValue(double d) {
        getPrefs().putDouble(getKey(), d);
    }

    public double getValue() {
        return Double.valueOf(getValueString()).doubleValue();
    }

    public double getDefault() {
        return Double.valueOf(getDefaultString()).doubleValue();
    }
}
