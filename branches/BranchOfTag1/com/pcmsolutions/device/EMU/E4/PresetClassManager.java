package com.pcmsolutions.device.EMU.E4;


import com.pcmsolutions.device.EMU.E4.preset.PresetModel;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:13:42
 * To change this template use Options | File Templates.
 */
public class PresetClassManager {
    private final static Vector profiles = new Vector();

    public static void addPresetClass(Class presetClass, String prefix, String name) {
        if (!PresetModel.class.isAssignableFrom(presetClass))
            throw new IllegalArgumentException("Invalid preset class - must implement PresetModel");

        profiles.add(new PresetClassProfile(presetClass, prefix, name));
    }

    public static PresetClassProfile[] getAllProfiles() {
        return (PresetClassProfile[]) ((Vector) profiles.clone()).toArray(new PresetClassProfile[profiles.size()]);
    }

    public static PresetClassProfile[] getAllProfilesWithNonNullPrefix() {
        Vector outProfiles = new Vector();
        for (int i = 0,j = profiles.size(); i < j; i++) {
            if (((PresetClassProfile) profiles.get(i)).getPrefix() != null)
                outProfiles.add(profiles.get(i));
        }
        return (PresetClassProfile[]) ((Vector) outProfiles.clone()).toArray(new PresetClassProfile[outProfiles.size()]);
    }

    public static PresetModel getMostDerivedPresetInstance(PresetModel baseClass, String name) throws InstantiationException, IllegalAccessException {
        Class[] ca = getAllPresetClasses(name);
        Class candidate = baseClass.getClass();
        for (int i = 0, n = ca.length; i < n; i++) {
            if (ca[i].isInstance(baseClass))         // ca[i] guaranteed non-null by virtue of addPresetClass semantics
                candidate = mostDerived(ca[i], candidate);
        }
        if (candidate != baseClass.getClass()) {
            PresetModel pm = (PresetModel) candidate.newInstance();
            pm.setPreset(baseClass.getPreset());
            pm.setPresetContext(baseClass.getPresetContext());
            return pm;
        }
        return baseClass;
    }

    private static Class[] getAllPresetClasses(String name) {
        Vector classes = new Vector();
        PresetClassProfile pcp;
        String prefix;
        for (int i = 0, n = profiles.size(); i < n; i++) {
            pcp = (PresetClassProfile) profiles.get(i);
            prefix = pcp.getPrefix();
            if (prefix == null || name.indexOf(prefix) == 0)
                classes.add(pcp.getPresetClass());
        }
        Class[] ca = new Class[classes.size()];
        return (Class[]) classes.toArray(ca);
    }

    private static Class mostDerived(Class a, Class b) {
        if (a == null && b != null)
            return b;
        if (a != null && b == null)
            return a;
        if (a == null && b == null)
            return a;

        if (a.isAssignableFrom(b))
            return b;

        return a;
    }

    public static class PresetClassProfile {
        private Class presetClass;
        private String prefix;
        private String name;

        public PresetClassProfile(Class presetClass, String prefix, String name) {
            this.prefix = prefix;
            this.presetClass = presetClass;
            this.name = name;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getName() {
            return name;
        }

        public Class getPresetClass() {
            return presetClass;
        }

        public PresetModel getInstance() {
            return null;
        }
    }
}
