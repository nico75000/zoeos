package com.pcmsolutions.system.preferences;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 05:47:06
 * To change this template use Options | File Templates.
 */
public class Impl_ZStringPref extends Impl_ZPref implements ZStringPref {
    public Impl_ZStringPref(Preferences prefs, String key, String def, String presentationName, String description) {
        super(prefs, key, def, presentationName, description);
    }

    public Impl_ZStringPref(Preferences prefs, String key, String def) {
        super(prefs, key, String.valueOf(def));
    }

    public Impl_ZStringPref(Preferences prefs, String key, String def, String presentationName, String description, String category) {
        super(prefs, key, def, presentationName, description, category);
    }

    public synchronized void putDefault() {
        getPrefs().put(getKey(), getDefaultString());
    }

    public Object getValueObject() {
        return getValueString();
    }

    public void putValueString(String strVal) {
        putValue(strVal);
    }

    public synchronized void putValue(String s) {
        getPrefs().put(getKey(), s);
    }

    public String getValue() {
        return getValueString();
    }
}
