package com.pcmsolutions.system.preferences;

import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 14-Feb-2004
 * Time: 13:52:56
 */
public class Impl_ZEnumPref extends Impl_ZStringPref implements ZEnumPref {
    private String[] legalValues;

    public Impl_ZEnumPref(Preferences prefs, String key, String[] values, String def, String presentationName, String description) {
        super(prefs, key, def, presentationName, description);
        this.legalValues = (String[]) values.clone();
    }

    public Impl_ZEnumPref(Preferences prefs, String key, String[] values,String def) {
        super(prefs, key, def);
        this.legalValues = (String[]) values.clone();
    }

    private int indexOfValue(String value) {
        return Arrays.asList(legalValues).indexOf(value);
    }

    public Impl_ZEnumPref(Preferences prefs, String key, String[] values, String def, String presentationName, String description, String category) {
        super(prefs, key, def, presentationName, description, category);
        this.legalValues = (String[]) values.clone();
    }

    public void putValueString(String strVal) {
        super.putValueString(strVal);
    }

    public synchronized void putValue(String s) {
        if (indexOfValue(s) == -1)
            return;
        super.putValue(s);
    }

    public String[] getLegalValues() {
        return (String[]) legalValues.clone();
    }
}
