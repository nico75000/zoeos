package com.pcmsolutions.system;

import com.pcmsolutions.system.preferences.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private static final String CAT_GUI = "GUI";

    public static final ZBoolPref ZPREF_boxStyleTabs = new Impl_ZBoolPref(zoeosPreferences, "boxStyleTabs", false, "Box style tabs", "Use boxed highlighting on selected tabs. Requires an application restart or re-opening of all tabbed views to have immediate global effect.", CAT_GUI);
    public static final ZBoolPref ZPREF_autoHuntAtStartup = new Impl_ZBoolPref(zoeosPreferences, "autoHuntAtStartup", true, "Auto-hunt at startup", "Automatically hunt for devices when ZoeOS starts up", CAT_MARSHALLING);
    public static final ZBoolPref ZPREF_stopHuntAtPending = new Impl_ZBoolPref(zoeosPreferences, "stopHuntAtPending", false, "Stop hunt at pending", "Leave devices found in hunt at pending state so user can start manually", CAT_MARSHALLING);
    public static final ZBoolPref ZPREF_serializeDeviceMarshalling = new Impl_ZBoolPref(zoeosPreferences, "serializeDeviceMarshalling", true, "Serialize device marshalling", "Marshall devices found during hunting one-by-one, rather than simultaneously", CAT_MARSHALLING);
    public static final ZBoolPref ZPREF_releaseMidiOnMinimize = new Impl_ZBoolPref(zoeosPreferences, "releaseMidiOnMinimize", false, "Midi release on minimize", "Instruct midi devices to release midi resources (ports) when the application is minimized", CAT_MIDI);
    public static final ZStringPref ZPREF_deviceClasses = new Impl_ZStringPref(zoeosPreferences, "deviceClasses", "com.pcmsolutions.device.EMU.E4");
    public static final ZIntPref ZPREF_zoeosUses = new Impl_ZIntPref(zoeosPreferences, "zoeosUses", 0);
    public static final ZBoolPref ZPREF_sideBarRollover = new Impl_ZBoolPref(zoeosPreferences, "useSideBarRollover", true, "Use rollover on side bars", "Use a rollover effect on docks in the sidebar", CAT_DOCKING);
    public static final ZBoolPref ZPREF_autoHideShowContentsHidden = new Impl_ZBoolPref(zoeosPreferences, "autoHideShowContentsHidden", false, "Hide contents during auto-hide", "Hide contents during the steps of an auto-hide", CAT_DOCKING);
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


    public static final ZIntPref ZPREF_animationStepDelay = new Impl_ZIntPref(zoeosPreferences, "animationStepDelay", 0, "Animation step delay(ms)", "How long (in ms) each step is during an auto-hide animation", CAT_DOCKING) {
        public int getMinValue() {
            return 0;
        }

        public int getMaxValue() {
            return 1000;
        }
    };

    public static List getPropertyList() {
        ArrayList props = new ArrayList();
        props.add(new ZProperty(ZPREF_boxStyleTabs));
        props.add(new ZProperty(ZPREF_autoHuntAtStartup));
        props.add(new ZProperty(ZPREF_stopHuntAtPending));
        props.add(new ZProperty(ZPREF_serializeDeviceMarshalling));
        props.add(new ZProperty(ZPREF_releaseMidiOnMinimize));
        props.add(new ZProperty(ZPREF_sideBarRollover));
        props.add(new ZProperty(ZPREF_animationSteps));
        props.add(new ZProperty(ZPREF_animationStepDelay));
        props.add(new ZProperty(ZPREF_animationInitDelay));
        props.add(new ZProperty(ZPREF_autoHideShowContentsHidden));
        Collections.sort(props, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = ((ZProperty) o1).getCategory().compareTo(((ZProperty) o2).getCategory());
                if (c == 0)
                    c = ((ZProperty) o1).getName().compareTo(((ZProperty) o2).getName());
                return c;
            }
        });
        return props;
    }

    public void zDispose() {
        ZPREF_boxStyleTabs.zDispose();
        ZPREF_autoHuntAtStartup.zDispose();
        ZPREF_stopHuntAtPending.zDispose();
        ZPREF_serializeDeviceMarshalling.zDispose();
        ZPREF_releaseMidiOnMinimize.zDispose();
        ZPREF_deviceClasses.zDispose();
        ZPREF_zoeosUses.zDispose();
        ZPREF_sideBarRollover.zDispose();
        ZPREF_animationSteps.zDispose();
        ZPREF_animationStepDelay.zDispose();
        ZPREF_animationInitDelay.zDispose();
        ZPREF_autoHideShowContentsHidden.zDispose();
    }
}
