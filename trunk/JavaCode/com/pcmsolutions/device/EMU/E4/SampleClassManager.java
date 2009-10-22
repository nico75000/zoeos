package com.pcmsolutions.device.EMU.E4;


import com.pcmsolutions.device.EMU.E4.sample.SampleModel;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:13:42
 * To change this template use Options | File Templates.
 */
class SampleClassManager {
    private final static ArrayList profiles = new ArrayList();

    public static void addSampleClass(Class sampleClass, String prefix) {
        if (!SampleModel.class.isAssignableFrom(sampleClass))
            throw new IllegalArgumentException("Invalid sample class - must implement SampleModel");
        if (!ReadableSample.class.isAssignableFrom(sampleClass))
            throw new IllegalArgumentException("Invalid sample class - must implement ReadableSample");

        profiles.add(new SampleClassProfile(sampleClass, prefix));
    }

    public static SampleModel getMostDerivedSampleInstance(SampleModel baseClass, String name) throws InstantiationException, IllegalAccessException {
        Class[] ca = getAllSampleClasses(name);
        Class candidate = baseClass.getClass();
        for (int i = 0, n = ca.length; i < n; i++) {
            if (ca[i].isInstance(baseClass))         // ca[i] guaranteed non-null by virtue of addSampleClass semantics
                candidate = mostDerived(ca[i], candidate);
        }
        if (candidate != baseClass.getClass()) {
            SampleModel pm = (SampleModel) candidate.newInstance();
            pm.setSample(baseClass.getSample());
            pm.setSampleContext(baseClass.getSampleContext());
            return (ReadableSample)pm;
        }
        return baseClass;
    }

    private static Class[] getAllSampleClasses(String name) {
        ArrayList classes = new ArrayList();
        SampleClassProfile pcp;
        String prefix;
        for (int i = 0, n = profiles.size(); i < n; i++) {
            pcp = (SampleClassProfile) profiles.get(i);
            prefix = pcp.getPrefix();
            if (prefix == null || name.indexOf(prefix) == 0)
                classes.add(pcp.getSampleClass());
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

    private static class SampleClassProfile {
        private Class sampleClass;
        private String prefix;

        public SampleClassProfile(Class sampleClass, String prefix) {
            this.prefix = prefix;
            this.sampleClass = sampleClass;
        }

        public String getPrefix() {
            return prefix;
        }

        public Class getSampleClass() {
            return sampleClass;
        }

        public SampleModel getInstance() {
            return null;
        }
    }
}
