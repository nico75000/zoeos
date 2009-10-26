package com.pcmsolutions.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Sep-2003
 * Time: 20:00:55
 * To change this template use Options | File Templates.
 */
public class ExternalSampleEditors {
    private static Preferences node = Preferences.userNodeForPackage(ExternalSampleEditors.class);
    private static final String EMPTY_EDITOR = "";

    public static interface ExternalEditor extends Comparable {
        public String getName();

        public String getLaunchString();

        public void launch(String parameterString) throws IOException;
    }

    public static synchronized ExternalEditor[] getEditors() {
        ArrayList editors = new ArrayList();
        try {
            final String[] childNames = node.childrenNames();

            for (int i = 0; i < childNames.length; i++) {
                final int f_i = i;
                final String s = node.get(childNames[i], EMPTY_EDITOR);

                if (s.equals(EMPTY_EDITOR))
                    continue;

                editors.add(new ExternalEditor() {
                    public String getName() {
                        return childNames[f_i];
                    }

                    public String toString() {
                        return getName();
                    }

                    public String getLaunchString() {
                        return s;
                    }

                    public void launch(String parameterString) throws IOException {
                        Runtime.getRuntime().exec(s + " " + parameterString);
                    }

                    public int compareTo(Object o) {
                        return getName().compareTo(o.toString());
                    }
                });
            }

        } catch (BackingStoreException e) {
            return new ExternalEditor[0];
        }

        return (ExternalEditor[]) editors.toArray(new ExternalEditor[editors.size()]);
    }

    public static void clear() {
        try {
            node.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void removeEditor(String name) {
        node.put(name, EMPTY_EDITOR);
    }

    public static synchronized void addEditor(String name, String launchString) {
        node.put(name, launchString);
        try {
            node.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
}
