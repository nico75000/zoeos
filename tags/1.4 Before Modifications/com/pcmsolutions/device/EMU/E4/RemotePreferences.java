package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZProperty;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.preferences.Impl_ZIntPref;
import com.pcmsolutions.system.preferences.ZIntPref;
import com.pcmsolutions.system.preferences.Impl_ZEnumPref;
import com.pcmsolutions.system.preferences.ZEnumPref;
import com.pcmsolutions.device.EMU.E4.remote.Remotable;

import javax.swing.event.ChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 03-Feb-2004
 * Time: 16:52:48
 */
public class RemotePreferences implements ZDisposable {
    public final ZIntPref ZPREF_commErrorThreshold;
    public final ZIntPref ZPREF_commPause;
    public final ZIntPref ZPREF_commTimeout;
    public final ZIntPref ZPREF_maxSmdiSampleRate;
    public final ZEnumPref ZPREF_smdiPacketSizeInKb;

    public static final Preferences staticPrefs = Preferences.userNodeForPackage(Remotable.class.getClass());

    private final Preferences remotePreferences;

    private static final int defCommErrorThreshold = 2;
    private static final int defUltraPause = 8;
    private static final int defClassicPause = 20;
    private static final int defCommTimeout = 3000;      // ms

    private static final String CAT_REMOTE = "Remote";
    private final static String CAT_SMDI = "SMDI";

    private final Vector propertyList = new Vector();

    public RemotePreferences(Preferences preferences, boolean ultra) {
        this.remotePreferences = preferences;

        ZPREF_smdiPacketSizeInKb = new Impl_ZEnumPref(remotePreferences, "smdiPacketSizeInKb", new String[]{"1", "2", "4", "8", "16", "32"}, "32", "SMDI packet size (Kb)", "Size of each data chunk (in Kbytes) when transferring audio over SMDI to this device", CAT_SMDI);
        ZPREF_maxSmdiSampleRate = new Impl_ZIntPref(remotePreferences, "maxSmdiSampleRate", 48000, "Max sample rate", "Maximum sample rate allowed during SMDI transfers to the Emulator. Audio at rates above this limit will be converted down. Generally you would leave this at 48Khz which is the maximum allowed by the Emulator.", CAT_SMDI) {
            public int getMinValue() {
                return 2000;
            }

            public int getMaxValue() {
                return 48000;
            }
        };

        ZPREF_commErrorThreshold = new Impl_ZIntPref(preferences, "commErrorThreshold", defCommErrorThreshold, "Comm error threshold", "Number of communication errors tolerated before device will be stopped", CAT_REMOTE) {
            public int getMinValue() {
                return 1;
            }

            public int getMaxValue() {
                return 20;
            }
        };
        ZPREF_commTimeout = new Impl_ZIntPref(preferences, "commTimeout", defCommTimeout, "Comm timeout (ms)", "Time interval (in ms) before the remote device is deemed not responding. Don't set this too low - it can play an important role in copying to preset flash. ", CAT_REMOTE) {
            public int getMinValue() {
                return 100;
            }

            public int getMaxValue() {
                return 5000;
            }

            public int getIncrementValue() {
                return 50;
            }
        };

        int dp;
        if (ultra)
            dp = defUltraPause;
        else
            dp = defClassicPause;
        ZPREF_commPause = new Impl_ZIntPref(preferences, "commPause", dp, "Comm pause (ms)", "Pause time (in ms) between sending consecutive messages to the remote device", CAT_REMOTE) {
            public int getMinValue() {
                return 5;
            }

            public int getMaxValue() {
                return 100;
            }

            public int getIncrementValue() {
                return 1;
            }
        };

        propertyList.add(new ZProperty(ZPREF_commErrorThreshold));
        propertyList.add(new ZProperty(ZPREF_commPause));
        propertyList.add(new ZProperty(ZPREF_commTimeout));
        propertyList.add(new ZProperty(ZPREF_smdiPacketSizeInKb));
        propertyList.add(new ZProperty(ZPREF_maxSmdiSampleRate));
        Collections.sort(propertyList, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = ((ZProperty) o1).getCategory().compareTo(((ZProperty) o2).getCategory());
                if (c == 0)
                    c = ((ZProperty) o1).getName().compareTo(((ZProperty) o2).getName());
                return c;
            }
        });
    }

    public void addGlobalChangeListener(ChangeListener cl) {
        for (int i = 0, j = propertyList.size(); i < j; i++)
            ((ZProperty) propertyList.get(i)).getZPref().addChangeListener(cl);
    }

    public void removeGlobalChangeListener(ChangeListener cl) {
        for (int i = 0, j = propertyList.size(); i < j; i++)
            ((ZProperty) propertyList.get(i)).getZPref().removeChangeListener(cl);
    }

    public Preferences getRemotePreferences() {
        return remotePreferences;
    }

    public List getPropertyList() {
        return (Vector) propertyList.clone();
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(propertyList);
        propertyList.clear();
    }
}
