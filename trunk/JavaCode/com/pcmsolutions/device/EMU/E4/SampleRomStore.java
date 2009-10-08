package com.pcmsolutions.device.EMU.E4;

import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Sep-2003
 * Time: 21:02:31
 * To change this template use Options | File Templates.
 */
public class SampleRomStore {
    private static final String PREF_NEXT_ROM_INDEX = "IndexForNextRom";
    private static final String MISSING_FIELD = "missing";
    private static final String PREF_ROM_NAME = "NAME";
    private static final String PREF_ROM_DESCRIPTION = "DESCRIPTION";
    private static final String PREF_ROM_ORIGINATOR = "ORIGINATOR";
    private static final Preferences prefs = Preferences.userNodeForPackage(SampleRomStore.class).node("Sample Rom Store");

    public synchronized static void addRom(String name, String desc, String originator, String[] sampleNames) throws BackingStoreException {
        if (sampleNames.length != DeviceContext.SAMPLE_ROM_SIZE || name == null || desc == null || originator == null)
            throw new IllegalArgumentException("invalid data for rom");

        if (isRomAlreadyStored(sampleNames))
            return;

        int nextRomIndex = prefs.getInt(PREF_NEXT_ROM_INDEX, 0);

        Preferences romPref = prefs.node(String.valueOf(nextRomIndex));

        for (int i = 0; i < DeviceContext.SAMPLE_ROM_SIZE; i++) {
            if (sampleNames[i] == null) {
                romPref.clear();
                return;
            }
            romPref.put(String.valueOf(i), sampleNames[i]);
        }

        romPref.put(PREF_ROM_NAME, name);
        romPref.put(PREF_ROM_DESCRIPTION, desc);
        romPref.put(PREF_ROM_ORIGINATOR, originator);
        prefs.putInt(PREF_NEXT_ROM_INDEX, nextRomIndex + 1);

        romPref.flush();
    }

    // may return null
    public synchronized static boolean isRomAlreadyStored(String[] sampleNames) throws BackingStoreException {
        int[] indexes = new int[sampleNames.length];

        for (int i = 0,j = sampleNames.length; i < j; i++)
            indexes[i] = i;

        return tryMatchRom(indexes, sampleNames, false).length > 0;
    }

    public synchronized static RomProfile[] tryMatchRom(int[] indexes, String[] sampleNames) throws BackingStoreException {
        return tryMatchRom(indexes, sampleNames, true);
    }

    // may return null
    private synchronized static RomProfile[] tryMatchRom(int[] indexes, String[] sampleNames, boolean checkAll) throws BackingStoreException {
        if (indexes.length != sampleNames.length)
            throw new IllegalArgumentException("invalid data for rom match");

        ArrayList matchedRoms = new ArrayList();

        String[] nodeNames = prefs.childrenNames();
        Preferences romPref;
        for (int i = 0,j = nodeNames.length; i < j; i++) {
            boolean match = true;
            romPref = prefs.node(nodeNames[i]);
            for (int k = 0,l = indexes.length; k < l; k++)
                if (!romPref.get(String.valueOf(indexes[k]), "").equals(sampleNames[k])) {
                    match = false;
                    break;
                }

            if (match) {
                final String[] sn = new String[DeviceContext.SAMPLE_ROM_SIZE];
                for (int z = 0; z < DeviceContext.SAMPLE_ROM_SIZE; z++)
                    sn[z] = romPref.get(String.valueOf(z), MISSING_FIELD);
                final String name = romPref.get(PREF_ROM_NAME, MISSING_FIELD);
                final String desc = romPref.get(PREF_ROM_DESCRIPTION, MISSING_FIELD);
                final String orig = romPref.get(PREF_ROM_ORIGINATOR, MISSING_FIELD);

                matchedRoms.add(new RomProfile() {
                    public String getName() {
                        return name;
                    }

                    public String getDescription() {
                        return desc;
                    }

                    public String getOriginator() {
                        return orig;
                    }

                    public String[] sampleNames() {
                        return sn;
                    }
                });
                if (!checkAll)
                    break;
            }
        }
        return (RomProfile[]) matchedRoms.toArray(new RomProfile[matchedRoms.size()]);
    }

    public interface RomProfile {
        public String getName();

        public String getDescription();

        public String getOriginator();

        public String[] sampleNames();
    }
}
