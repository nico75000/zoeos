package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.*;
import com.pcmsolutions.system.preferences.*;
import com.pcmsolutions.device.EMU.DeviceException;

import javax.swing.event.ChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
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
    private final static String CAT_AUDITIONING = "Auditioning";

    public static final String SESSION_RESTORE_MODE_ALWAYS = "always";
    public static final String SESSION_RESTORE_MODE_ASK = "ask";
    public static final String SESSION_RESTORE_MODE_NEVER = "never";

    public final static String ENVELOPE_MODE_FIXED = "FIXED";
    //public final static String MODE_FIXED_ZOOMED = "ZOOMED";
    public final static String ENVELOPE_MODE_SCALED = "SCALED";

    // INSTANCE PREFERENCES
    private final Preferences devicePreferences;
    // private final DeviceContext device;

    public final ZBoolPref ZPREF_askToOpenAfterPresetCopy;
    public final ZBoolPref ZPREF_voiceDoubleClickEdits;
    public final ZBoolPref ZPREF_syncPalettes;
    //public final ZBoolPref ZPREF_useRomMatching;
    public final ZBoolPref ZPREF_expandingZonesByDefault;
    public final ZEnumPref ZPREF_sessionRestoreMode;
    public final ZBoolPref ZPREF_alwaysReloadROMSamples;
    public final ZBoolPref ZPREF_alwaysReloadFlashPresets;
    public final ZBoolPref ZPREF_reopenPreviousEditors;
    public final ZBoolPref ZPREF_usePartitionedVoiceEditing;
    //public final ZBoolPref ZPREF_usePartitionedPresetEditing;
    public final ZBoolPref ZPREF_groupEnvelopesWhenVoiceTabbed;
    public final ZBoolPref ZPREF_showLinkFilterSection;

    public final ZIntPref ZPREF_auditionChnl;
    public final ZIntPref ZPREF_auditionChnlVol;
    public final ZIntPref ZPREF_quickAuditionVel;
    public final ZIntPref ZPREF_quickAuditionGate;
    public final ZEnumPref ZPREF_quickAuditionNote;
    public final ZIntPref ZPREF_sampleAuditionPreset;
   // public final ZBoolPref ZPREF_allNotesOffBetweenAuditions;
    public final ZBoolPref ZPREF_enableAuditioning;
    public final ZBoolPref ZPREF_truncateDelayDuringVoiceAudition;
    public final ZBoolPref ZPREF_disableLatchDuringVoiceAudition;
    //public final ZBoolPref ZPREF_serializeOverlappingAuditions;
    //public final ZBoolPref ZPREF_syncPresetEditingToAudition;

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

    public static final String ZPREF_sessionRestoreMode_key = "bankRestroeMode";

    static {
        staticPreferences = Preferences.userNodeForPackage(DevicePreferences.class.getClass());
        ZPREF_voiceTableUserIds = new Impl_ZStringPref(staticPreferences, "voiceTableUserIds", "", "Voice table user parameters", "User specified parametera to display on voice tables", CAT_PRESET_EDITING);

        ZPREF_fillAmpEnvelopes = new Impl_ZBoolPref(staticPreferences, "fillAmpEnvelopes", false, "Fill amp envelope", "Fill out the sections of amplifier envelopes", CAT_ENVELOPES);
        ZPREF_fillFilterEnvelopes = new Impl_ZBoolPref(staticPreferences, "fillFilterEnvelopes", false, "Fill filter envelope", "Fill out the sections of filter envelopes", CAT_ENVELOPES);
        ZPREF_fillAuxEnvelopes = new Impl_ZBoolPref(staticPreferences, "fillAuxEnvelopes", true, "Fill aux envelope", "Fill out the sections of auxillary envelopes", CAT_ENVELOPES);

        ZPREF_ampEnvelopeMode = new Impl_ZEnumPref(staticPreferences, "ampEnvelopeMode", new String[]{ENVELOPE_MODE_FIXED, ENVELOPE_MODE_SCALED}, ENVELOPE_MODE_SCALED, "Amp envelope mode", "Display mode (scaled or fixed) for amplifier envelopes", CAT_ENVELOPES);
        ZPREF_filterEnvelopeMode = new Impl_ZEnumPref(staticPreferences, "filterEnvelopeMode", new String[]{ENVELOPE_MODE_FIXED, ENVELOPE_MODE_SCALED}, ENVELOPE_MODE_SCALED, "Filter envelope mode", "Display mode (scaled or fixed) for filter envelopes", CAT_ENVELOPES);
        ZPREF_auxEnvelopeMode = new Impl_ZEnumPref(staticPreferences, "auxEnvelopeMode", new String[]{ENVELOPE_MODE_FIXED, ENVELOPE_MODE_SCALED}, ENVELOPE_MODE_SCALED, "Aux envelope mode", "Display mode (scaled or fixed) for auxillary envelopes", CAT_ENVELOPES);
    }

    DeviceContext device = null;

    public DevicePreferences(Preferences prefs) {
        this.devicePreferences = prefs;
        ZPREF_askToOpenAfterPresetCopy = new Impl_ZBoolPref(prefs, "askToOpenAfterPresetCopy", true, "Ask to open after a single preset copy or preset drop", "Ask to open destination preset after a preset copy", CAT_PRESET_EDITING);
        //ZPREF_useRomMatching = new Impl_ZBoolPref(prefs, "useRomMatching", true, "Use ROM Matching", "Use a time saving matching algorithm to determine what sample ROMs are installed. If a whole session or Rom data from a previous session is being restored, this property will be ignored", CAT_DEVICE);
        ZPREF_expandingZonesByDefault = new Impl_ZBoolPref(prefs, "expandingZonesByDefault", false, "Expand zones by default", "Expand all zones automatically when opening a preset viewer/editor", CAT_PRESET_EDITING);
        ZPREF_sessionRestoreMode = new Impl_ZEnumPref(prefs, ZPREF_sessionRestoreMode_key, new String[]{SESSION_RESTORE_MODE_ALWAYS, SESSION_RESTORE_MODE_ASK, SESSION_RESTORE_MODE_NEVER}, SESSION_RESTORE_MODE_ASK, "Restore previous session", "Restore data and views from the previous session", CAT_DEVICE);
        ZPREF_alwaysReloadROMSamples = new Impl_ZBoolPref(prefs, "alwaysReloadROMSamples", true, "Always reload ROM samples", "Always reload ROM sample names from the previous session. If a whole session is being restored, this property is ignored.", CAT_DEVICE);
        ZPREF_alwaysReloadFlashPresets = new Impl_ZBoolPref(prefs, "alwaysReloadFlashPresets", true, "Always reload Flash presets", "Always reload Flash presets from the previous session.  If a whole session is being restored, this property is ignored.", CAT_DEVICE);
        ZPREF_reopenPreviousEditors = new Impl_ZBoolPref(prefs, "reopenPreviousEditors", true, "Reopen editors from previous session", "Reopen editors during a previous session restore. If session restore is false, this property is ignored.", CAT_DEVICE);
        ZPREF_voiceDoubleClickEdits = new Impl_ZBoolPref(prefs, "voiceDoubleClickedits", true, "Double click voice editing", "Double click a voice to edit", CAT_PRESET_EDITING);
        ZPREF_usePartitionedVoiceEditing = new Impl_ZBoolPref(prefs, "useTabbedVoicePanel", true, "Partitioned voice editing", "Edit voices by partitioning each section.", CAT_PRESET_EDITING);
        //ZPREF_usePartitionedPresetEditing = new Impl_ZBoolPref(prefs, "usePartitionedPresetEditing", true, "Partitioned preset editing", "Edit presets by partitioning the voices, links and global sections.", CAT_PRESET_EDITING);
        ZPREF_groupEnvelopesWhenVoiceTabbed = new Impl_ZBoolPref(prefs, "groupEnvelopesWhenVoiceTabbed", true, "Group envelopes", "Group envelopes on one tab when using partitioned voice editing", CAT_PRESET_EDITING);
        ZPREF_showLinkFilterSection = new Impl_ZBoolPref(prefs, "showLinkFilterSection", true, "Show link filters", "Show filters on the link table", CAT_PRESET_EDITING);
        ZPREF_syncPalettes = new Impl_ZBoolPref(prefs, "syncPalettes", true, "Sync palettes", "When an editor belonging to a device is activated in the workspace, activate the corresponding device palettes in the docking framework", CAT_DEVICE_WORKSPACE);
        ZPREF_auditionChnl = new Impl_ZIntPref(prefs, "presetAuditionChannel", 16, "Midi channel for auditioning", "Midi channel on which to audition. If this device has the 32 midi channel expansion and ZoeOS is communicating on the Midi In B of the device, then subject to a SMDI connection, ZoeOS will be able to audition on all 32 channels.", CAT_AUDITIONING) {
            public int getMinValue() {
                return 1;
            }

            public int getMaxValue() {
                int chnls = 16;
                try {
                    chnls = (device == null ? 16 : device.getAuditionManager().getMaxAudChannel());
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
                return chnls;
            }
        };
        ZPREF_quickAuditionNote = new Impl_ZEnumPref(prefs, "presetAuditionNote", NoteUtilities.Note.getAllNoteStrings(), NoteUtilities.Note.getNote(60).getNoteString(), "Note for audition", "Note to play during auditioning", CAT_AUDITIONING);
        ZPREF_quickAuditionGate = new Impl_ZIntPref(prefs, "presetAuditionGate", 1000, "Gate time (ms) for audition", "Time (in ms) that audition note will sound for.", CAT_AUDITIONING) {
            public int getMinValue() {
                return 1;       
            }

            public int getMaxValue() {
                return 10000;
            }
        };
        ZPREF_quickAuditionVel = new Impl_ZIntPref(prefs, "presetQuickAuditionVel", 100, "Note velocity for audition", "Velocity value of auditioning note-on", CAT_AUDITIONING) {
            public int getMinValue() {
                return 0;
            }

            public int getMaxValue() {
                return 127;
            }
        };
        ZPREF_auditionChnlVol = new Impl_ZIntPref(prefs, "presetAuditionChnlVol", 100, "Channel volume for audition", "Midi volume for audition channel", CAT_AUDITIONING) {
            public int getMinValue() {
                return 0;
            }

            public int getMaxValue() {
                return 127;
            }
        };
        ZPREF_sampleAuditionPreset = new Impl_ZIntPref(prefs, "sampleAuditionPreset", 988, "Preset location for auditioning", "Preset location to be used during auditioning. Any user preset at this location will potentially be destroyed - set it to some high preset location that is unlikely to be in use.", CAT_AUDITIONING) {
            public int getMinValue() {
                return 0;
            }

            public int getMaxValue() {
                return 999;
            }
        };
       // ZPREF_syncPresetEditingToAudition = new Impl_ZBoolPref(prefs, "syncPresetEditingToAudition", false, "Sync preset editing to audition channel", "When a preset is activated for editing, move it to the preset audition channel", CAT_AUDITIONING);
        //ZPREF_allNotesOffBetweenAuditions = new Impl_ZBoolPref(prefs, "allNotesOffBetweenAuditions", false, "Send all notes off between auditions", "Send a midi 'AllNotesOff' between successive auditions", CAT_AUDITIONING);
        //ZPREF_serializeOverlappingAuditions = new Impl_ZBoolPref(prefs, "serializeOverlappingAuditions", true, "Serialize overlapping auditions", "Prevents note-off clipping due to overlapping auditions on the same note", CAT_AUDITIONING);
        ZPREF_enableAuditioning = new Impl_ZBoolPref(prefs, "enableAuditioning", true, "Enable auditioning", "Enable/disable midi auditioning", CAT_AUDITIONING);
        ZPREF_truncateDelayDuringVoiceAudition = new Impl_ZBoolPref(prefs, "truncateVoiceDelayDuringAuditioning", true, "Truncate voice delay during voice audition", "Zero the voice delay parameter when auditioning voices. Multiple voices auditioned in stepped mode always have delay truncation regardless of this property.", CAT_AUDITIONING);
        ZPREF_disableLatchDuringVoiceAudition = new Impl_ZBoolPref(prefs, "disableLatchDuringVoiceAudition", true, "Disable latch on voices (if any) prior to voice audition", "Disable the voice latch parameter when auditioning voices.", CAT_AUDITIONING);

        propertyList.add(new ZProperty(ZPREF_syncPalettes));
        //propertyList.add(new ZProperty(ZPREF_useRomMatching));
        propertyList.add(new ZProperty(ZPREF_expandingZonesByDefault));
        propertyList.add(new ZProperty(ZPREF_alwaysReloadROMSamples));
        propertyList.add(new ZProperty(ZPREF_alwaysReloadFlashPresets));
        propertyList.add(new ZProperty(ZPREF_reopenPreviousEditors));
        propertyList.add(new ZProperty(ZPREF_voiceDoubleClickEdits));
        propertyList.add(new ZProperty(ZPREF_usePartitionedVoiceEditing));
        //propertyList.add(new ZProperty(ZPREF_usePartitionedPresetEditing));
        propertyList.add(new ZProperty(ZPREF_groupEnvelopesWhenVoiceTabbed));
        propertyList.add(new ZProperty(ZPREF_sessionRestoreMode));
        propertyList.add(new ZProperty(ZPREF_showLinkFilterSection));
        propertyList.add(new ZProperty(ZPREF_auditionChnl));
        propertyList.add(new ZProperty(ZPREF_quickAuditionNote));
        propertyList.add(new ZProperty(ZPREF_sampleAuditionPreset));
        //propertyList.add(new ZProperty(ZPREF_syncPresetEditingToAudition));
        propertyList.add(new ZProperty(ZPREF_askToOpenAfterPresetCopy));
        propertyList.add(new ZProperty(ZPREF_quickAuditionGate));
        propertyList.add(new ZProperty(ZPREF_quickAuditionVel));
       // propertyList.add(new ZProperty(ZPREF_allNotesOffBetweenAuditions));
       // propertyList.add(new ZProperty(ZPREF_serializeOverlappingAuditions));
        propertyList.add(new ZProperty(ZPREF_auditionChnlVol));
        propertyList.add(new ZProperty(ZPREF_truncateDelayDuringVoiceAudition));
        propertyList.add(new ZProperty(ZPREF_disableLatchDuringVoiceAudition));
        propertyList.add(new ZProperty(ZPREF_enableAuditioning));

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

    public List getPropertyList() {
        return (Vector) propertyList.clone();
    }

    public Preferences getPreferences() {
        return devicePreferences;
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(propertyList);
        propertyList.clear();
    }
}
