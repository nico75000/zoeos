package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZProperty;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.preferences.*;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 10-Feb-2004
 * Time: 15:59:24
 */
public class DevicePreferences implements ZDisposable {
    private final static String CAT_PRESET_EDITING = "Preset Editing";
    private final static String CAT_DEVICE = "Device";
    private final static String CAT_DEVICE_WORKSPACE = "Device Workspace";
    private final static String CAT_ENVELOPES = "Envelopes";

    public static final String SESSION_RESTORE_MODE_ALWAYS = "always";
    public static final String SESSION_RESTORE_MODE_ASK = "ask";
    public static final String SESSION_RESTORE_MODE_NEVER = "never";

    public final static String ENVELOPE_MODE_FIXED = "FIXED";
    //public final static String MODE_FIXED_ZOOMED = "ZOOMED";
    public final static String ENVELOPE_MODE_SCALED = "SCALED";

    // INSTANCE PREFERENCES
    private final Preferences devicePreferences;

    public final ZBoolPref ZPREF_voiceDoubleClickEdits;
    public final ZBoolPref ZPREF_syncPalettes;
    public final ZBoolPref ZPREF_useRomMatching;
    public final ZBoolPref ZPREF_expandingZonesByDefault;
    public final ZEnumPref ZPREF_sessionRestoreMode;
    public final ZBoolPref ZPREF_alwaysReloadROMSamples;
    public final ZBoolPref ZPREF_alwaysReloadFlashPresets;
    public final ZBoolPref ZPREF_useTabbedVoicePanel;
    public final ZBoolPref ZPREF_groupEnvelopesWhenVoiceTabbed;
    public final ZBoolPref ZPREF_showLinkFilterSection;

    private final Vector propertyList = new Vector();

    // STATIC PREFERENCES
    private static final Preferences staticPreferences;
    public static final ZStringPref ZPREF_voiceTableUserIds;
    public static final ZBoolPref ZPREF_fillAmpEnvelopes;
    public static final ZBoolPref ZPREF_fillFilterEnvelopes;
    public static final ZBoolPref ZPREF_fillAuxEnvelopes;
    public static final ZEnumPref ZPREF_ampEnvelopeMode;
    public static final ZEnumPref ZPREF_filterEnvelopeMode;
    public static final ZEnumPref ZPREF_auxEnvelopeMode;

    static {
        staticPreferences = Preferences.userNodeForPackage(DevicePreferences.class);
        ZPREF_voiceTableUserIds = new Impl_ZStringPref(staticPreferences, "voiceTableUserIds", "", "Voice table user parameters", "User specified parametera to display on voice tables", CAT_PRESET_EDITING);

        ZPREF_fillAmpEnvelopes = new Impl_ZBoolPref(staticPreferences, "fillAmpEnvelopes", false, "Fill amp envelope", "Fill out the sections of amplifier envelopes", CAT_ENVELOPES);
        ZPREF_fillFilterEnvelopes = new Impl_ZBoolPref(staticPreferences, "fillFilterEnvelopes", false, "Fill filter envelope", "Fill out the sections of filter envelopes", CAT_ENVELOPES);
        ZPREF_fillAuxEnvelopes = new Impl_ZBoolPref(staticPreferences, "fillAuxEnvelopes", true, "Fill aux envelope", "Fill out the sections of auxillary envelopes", CAT_ENVELOPES);

        ZPREF_ampEnvelopeMode = new Impl_ZEnumPref(staticPreferences, "ampEnvelopeMode", new String[]{ENVELOPE_MODE_FIXED, ENVELOPE_MODE_SCALED}, ENVELOPE_MODE_SCALED, "Amp envelope mode", "Display mode (scaled or fixed) for amplifier envelopes", CAT_ENVELOPES);
        ZPREF_filterEnvelopeMode = new Impl_ZEnumPref(staticPreferences, "filterEnvelopeMode", new String[]{ENVELOPE_MODE_FIXED, ENVELOPE_MODE_SCALED}, ENVELOPE_MODE_SCALED, "Filter envelope mode", "Display mode (scaled or fixed) for filter envelopes", CAT_ENVELOPES);
        ZPREF_auxEnvelopeMode = new Impl_ZEnumPref(staticPreferences, "auxEnvelopeMode", new String[]{ENVELOPE_MODE_FIXED, ENVELOPE_MODE_SCALED}, ENVELOPE_MODE_SCALED, "Aux envelope mode", "Display mode (scaled or fixed) for auxillary envelopes", CAT_ENVELOPES);
    }

    public DevicePreferences(Preferences prefs) {
        this.devicePreferences = prefs;
        ZPREF_useRomMatching = new Impl_ZBoolPref(prefs, "useRomMatching", true, "Use ROM Matching", "Use a time saving matching algorithm to determine what sample ROMs are installed. If a whole session or Rom data from a previous session is being restored, this property will be ignored", CAT_DEVICE);
        ZPREF_expandingZonesByDefault = new Impl_ZBoolPref(prefs, "expandingZonesByDefault", false, "Expand zones by default", "Expand all zones automatically when opening a preset viewer/editor", CAT_PRESET_EDITING);
        ZPREF_sessionRestoreMode = new Impl_ZEnumPref(prefs, "bankRestoreMode", new String[]{SESSION_RESTORE_MODE_ALWAYS, SESSION_RESTORE_MODE_ASK, SESSION_RESTORE_MODE_NEVER}, SESSION_RESTORE_MODE_ASK, "Restore previous session", "Restore data and views from the previous session", CAT_DEVICE);
        ZPREF_alwaysReloadROMSamples = new Impl_ZBoolPref(prefs, "alwaysReloadROMSamples", true, "Always reload ROM samples", "Always reload ROM sample names from the previous session. If a whole session is being restored, this property is ignored.", CAT_DEVICE);
        ZPREF_alwaysReloadFlashPresets = new Impl_ZBoolPref(prefs, "alwaysReloadFlashPresets", true, "Always reload Flash presets", "Always reload Flash presets from the previous session.  If a whole session is being restored, this property is ignored.", CAT_DEVICE);
        ZPREF_voiceDoubleClickEdits = new Impl_ZBoolPref(prefs, "voiceDoubleClickedits", true, "Double click voice editing", "Double click a voice to edit", CAT_PRESET_EDITING);
        ZPREF_useTabbedVoicePanel = new Impl_ZBoolPref(prefs, "useTabbedVoicePanel", false, "Tabbed voice editing", "Edit voices using tabs for each section", CAT_PRESET_EDITING);
        ZPREF_groupEnvelopesWhenVoiceTabbed = new Impl_ZBoolPref(prefs, "groupEnvelopesWhenVoiceTabbed", true, "Group envelopes", "Group envelopes on one tab when using tabbed voice editing", CAT_PRESET_EDITING);
        ZPREF_showLinkFilterSection = new Impl_ZBoolPref(prefs, "showLinkFilterSection", true, "Show link filters", "Show filters on the link table", CAT_PRESET_EDITING);
        ZPREF_syncPalettes = new Impl_ZBoolPref(prefs, "syncPalettes", true, "Sync palettes", "When an editor belonging to a device is activated in the workspace, activate the corresponding device palettes in the docking framework", CAT_DEVICE_WORKSPACE);

        propertyList.add(new ZProperty(ZPREF_syncPalettes));
        propertyList.add(new ZProperty(ZPREF_useRomMatching));
        propertyList.add(new ZProperty(ZPREF_expandingZonesByDefault));
        propertyList.add(new ZProperty(ZPREF_alwaysReloadROMSamples));
        propertyList.add(new ZProperty(ZPREF_alwaysReloadFlashPresets));
        propertyList.add(new ZProperty(ZPREF_voiceDoubleClickEdits));
        propertyList.add(new ZProperty(ZPREF_useTabbedVoicePanel));
        propertyList.add(new ZProperty(ZPREF_groupEnvelopesWhenVoiceTabbed));
        propertyList.add(new ZProperty(ZPREF_sessionRestoreMode));
        propertyList.add(new ZProperty(ZPREF_showLinkFilterSection));

        Collections.sort(propertyList, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = ((ZProperty) o1).getCategory().compareTo(((ZProperty) o2).getCategory());
                if (c == 0)
                    c = ((ZProperty) o1).getName().compareTo(((ZProperty) o2).getName());
                return c;
            }
        });
    }

    public List getPropertyList() {
        return (Vector) propertyList.clone();
    }

    public Preferences getDevicePreferences() {
        return devicePreferences;
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(propertyList);
        propertyList.clear();
    }
}
