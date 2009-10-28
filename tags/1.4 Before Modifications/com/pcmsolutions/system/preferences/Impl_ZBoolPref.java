package com.pcmsolutions.system.preferences;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:47:06
 * To change this template use Options | File Templates.
 */
public class Impl_ZBoolPref extends Impl_ZPref implements ZBoolPref {
    public Impl_ZBoolPref(Preferences prefs, String key, boolean def, String presentationName, String description) {
        super(prefs, key, Boolean.valueOf(def).toString(), presentationName, description);
    }

    public Impl_ZBoolPref(Preferences prefs, String key, boolean def, String presentationName, String description, String category) {
        super(prefs, key, Boolean.valueOf(def).toString(), presentationName, description, category);
    }

    public Impl_ZBoolPref(Preferences prefs, String key, boolean def) {
        super(prefs, key, String.valueOf(def));
    }

    public synchronized void putDefault() {
        getPrefs().putBoolean(getKey(), Boolean.valueOf(getDefaultString()).booleanValue());
    }

    public void putValueString(String strVal) {
        putValue(Boolean.valueOf(strVal));
    }

    public Object getValueObject() {
        return Boolean.valueOf(getValueString());
    }

    public synchronized void putValue(Boolean b) {
        putValue(b.booleanValue());
    }

    public synchronized void toggleValue() {
        getPrefs().putBoolean(getKey(), !getValue());
    }

    public synchronized void putValue(boolean b) {
        getPrefs().putBoolean(getKey(), b);
    }

    public boolean getValue() {
        return Boolean.valueOf(getValueString()).booleanValue();
    }

    public boolean getDefault() {
        return Boolean.valueOf(getDefaultString()).booleanValue();
    }
}
