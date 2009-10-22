package com.pcmsolutions.system;

import com.pcmsolutions.system.preferences.*;

import javax.swing.event.ChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 10-Feb-2004
 * Time: 17:51:48
 */
public class ZoeosPreferences implements ZDisposable {

    private static final Preferences zoeosPreferences = Preferences.userNodeForPackage(Zoeos.class);

    private static final String CAT_DOCKING = "Docking";
    private static final String CAT_MARSHALLING = "Device Marshalling";
    private static final String CAT_MIDI = "Midi";
    private static final String CAT_GUI = "gui";
    private static final String CAT_GENERAL = "General";

    public static final ZBoolPref ZPREF_useRFXSubmixHeuristics = new Impl_ZBoolPref(zoeosPreferences, "useRFXSubmixHeuristics", true, "Allow CC79 selection of extended RFX busses", "Selection of extended RFX busses (Bus8 .. GFX2) in multimode is not supported via SysEx, but can be enabled via CC79 - use this method with reachable multi-mode channels on RFX enabled Emulators.", CAT_GENERAL);
    public static final ZBoolPref ZPREF_showTipsAtStartup = new Impl_ZBoolPref(zoeosPreferences, "showTipsAtStartup", true, "Show tips at startup", "Show Tip of the Day dialog when the application starts-up.", CAT_GENERAL);
    public static final ZBoolPref ZPREF_boxStyleTabs = new Impl_ZBoolPref(zoeosPreferences, "boxStyleTabs", false, "Box style tabs", "Use boxed highlighting on selected tabs. Requires an application restart or re-opening of all tabbed views to have immediate global effect.", CAT_GUI);
    public static final ZBoolPref ZPREF_autoHuntAtStartup = new Impl_ZBoolPref(zoeosPreferences, "autoHuntAtStartup", true, "Auto-hunt at startup", "Automatically hunt for devices when ZoeOS starts up", CAT_MARSHALLING);
    public static final ZBoolPref ZPREF_stopHuntAtPending = new Impl_ZBoolPref(zoeosPreferences, "stopHuntAtPending", false, "Stop hunt at pending", "Leave devices found in hunt at pending state so user can start manually", CAT_MARSHALLING);
    public static final ZBoolPref ZPREF_serializeDeviceMarshalling = new Impl_ZBoolPref(zoeosPreferences, "serializeDeviceMarshalling", true, "Serialize device marshalling", "Marshall devices found during hunting one-by-one, rather than simultaneously", CAT_MARSHALLING);
    public static final ZBoolPref ZPREF_releaseMidiOnMinimize = new Impl_ZBoolPref(zoeosPreferences, "releaseMidiOnMinimize", false, "Midi release on minimize", "Instruct midi devices to release midi resources (ports) when the application is minimized", CAT_MIDI);
    public static final ZStringPref ZPREF_deviceClasses = new Impl_ZStringPref(zoeosPreferences, "deviceClasses", "com.pcmsolutions.device.EMU.E4");
    public static final ZIntPref ZPREF_zoeosUses = new Impl_ZIntPref(zoeosPreferences, "zoeosUses", 0);
    public static final ZIntPref ZPREF_zoeosVersionUses = new Impl_ZIntPref(zoeosPreferences, "zoeosVersionUses" + Zoeos.version, 0);
    public static final ZBoolPref ZPREF_sideBarRollover = new Impl_ZBoolPref(zoeosPreferences, "useSideBarRollover", false, "Use rollover on side bars", "Use a rollover effect on docks in the sidebar", CAT_DOCKING);
    public static final ZBoolPref ZPREF_autoHideShowContentsHidden = new Impl_ZBoolPref(zoeosPreferences, "autoHideShowContentsHidden", false, "Hide contents during auto-hide", "Hide contents during the steps of an auto-hide", CAT_DOCKING);
    public static final ZDoublePref ZPREF_systemTempo = new Impl_ZDoublePref(zoeosPreferences, "systemTempo", 120.0, "System tempo in BPM", "Tempo");

    public static final ZIntPref ZPREF_animationInitDelay = new Impl_ZIntPref(zoeosPreferences, "animationInitialDelay", 10, "Animation initial delay(ms)", "How long (in ms) before an auto-hide animation will begin", CAT_DOCKING) {
        public int getMinValue() {
            return 10;
        }

        public int getMaxValue() {
            return 1000;
        }
    };
    public static final ZIntPref ZPREF_animationSteps = new Impl_ZIntPref(zoeosPreferences, "animationSteps", 1, "Animation steps", "How many steps when unhiding an auto-hidden dock", CAT_DOCKING) {
        public int getMinValue() {
            return 1;
        }

        public int getMaxValue() {
            return 100;
        }
    };


    public static final ZIntPref ZPREF_animationStepDelay = new Impl_ZIntPref(zoeosPreferences, "animationStepDelay", 5, "Animation step delay(ms)", "How long (in ms) each step is during an auto-hide animation", CAT_DOCKING) {
        public int getMinValue() {
            return 0;
        }

        public int getMaxValue() {
            return 1000;
        }
    };

    static Vector propertyList = new Vector();

    static {
        propertyList.add(new ZProperty(ZPREF_systemTempo));
        propertyList.add(new ZProperty(ZPREF_boxStyleTabs));
        propertyList.add(new ZProperty(ZPREF_showTipsAtStartup));
        propertyList.add(new ZProperty(ZPREF_autoHuntAtStartup));
        propertyList.add(new ZProperty(ZPREF_stopHuntAtPending));
        propertyList.add(new ZProperty(ZPREF_serializeDeviceMarshalling));
        propertyList.add(new ZProperty(ZPREF_releaseMidiOnMinimize));
        propertyList.add(new ZProperty(ZPREF_sideBarRollover));
        propertyList.add(new ZProperty(ZPREF_animationSteps));
        propertyList.add(new ZProperty(ZPREF_animationStepDelay));
        propertyList.add(new ZProperty(ZPREF_animationInitDelay));
        propertyList.add(new ZProperty(ZPREF_autoHideShowContentsHidden));
        //propertyList.add(new ZProperty(ZPREF_useRFXSubmixHeuristics));
        Collections.sort(propertyList, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = ((ZProperty) o1).getCategory().compareTo(((ZProperty) o2).getCategory());
                if (c == 0)
                    c = ((ZProperty) o1).getName().compareTo(((ZProperty) o2).getName());
                return c;
            }
        });
    }

    public static void addGlobalChangeListener(ChangeListener cl) {
        synchronized (propertyList) {
        for (int i = 0, j = propertyList.size(); i < j; i++)
            ((ZProperty) propertyList.get(i)).getZPref().addChangeListener(cl);
        }
    }

    public static void removeGlobalChangeListener(ChangeListener cl) {
        synchronized (propertyList) {
            for (int i = 0, j = propertyList.size(); i < j; i++)
                ((ZProperty) propertyList.get(i)).getZPref().removeChangeListener(cl);
        }
    }

    public static List getPropertyList() {
        return (Vector) propertyList.clone();
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(propertyList);
    }
}
