package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZProperty;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.preferences.Impl_ZIntPref;
import com.pcmsolutions.system.preferences.ZIntPref;

import java.util.*;
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

    public static final Preferences staticPrefs = Preferences.userNodeForPackage(Remotable.class);

    private final Preferences remotePreferences;

    private static final int defCommErrorThreshold = 2;
    private static final int defUltraPause = 8;
    private static final int defClassicPause = 25;
    private static final int defCommTimeout = 3000;      // ms

    private static final String CAT_REMOTE = "Remote";

    private final Vector propertyList = new Vector();

    public RemotePreferences(Preferences preferences, boolean ultra) {
        this.remotePreferences = preferences;
        ZPREF_commErrorThreshold = new Impl_ZIntPref(preferences, "commErrorThreshold", defCommErrorThreshold, "Comm error threshold", "Number of communication errors tolerated before device will be stopped", CAT_REMOTE) {
            public int getMinValue() {
                return 1;
            }

            public int getMaxValue() {
                return 20;
            }
        };
        ZPREF_commTimeout = new Impl_ZIntPref(preferences, "commTimeout", defCommTimeout, "Comm timeout(ms)", "Time interval (in ms) before the remote device is deemed not responding ", CAT_REMOTE) {
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
        ZPREF_commPause = new Impl_ZIntPref(preferences, "commPause", dp, "Comm pause(ms)", "Pause time (in ms) between sending consecutive messages to the remote device", CAT_REMOTE) {
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
        Collections.sort(propertyList, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = ((ZProperty) o1).getCategory().compareTo(((ZProperty) o2).getCategory());
                if (c == 0)
                    c = ((ZProperty) o1).getName().compareTo(((ZProperty) o2).getName());
                return c;
            }
        });
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
