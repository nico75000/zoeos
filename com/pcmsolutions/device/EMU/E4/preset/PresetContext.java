/*
 * PresetContext.java
 *
 * Created on January 3, 2003, 9:10 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.util.IntegerUseMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 *
 * @author  pmeehan
 */
public interface PresetContext {
    public static final Preferences prefs = Preferences.userNodeForPackage(PresetContext.class).node("PresetContext");
    public static final String PREF_autoGroup = "autoGroup";
    public static final String PREF_autoGroupAtTail = "autoGroupAtTail";
    public static final String PREF_tryMatchAppliedSamples = "tryMatchAppliedSamples";
    public static final ZBoolPref ZPREF_tryMatchAppliedSamples = new Impl_ZBoolPref(prefs, PREF_tryMatchAppliedSamples, true);

    public PresetContext getDelegatingPresetContext();

    public String getDeviceString();

    public DeviceParameterContext getDeviceParameterContext();

    public DeviceContext getDeviceContext();

    public SampleContext getRootSampleContext();

    // EVENTS
    public void addPresetContextListener(PresetContextListener pl);

    public void removePresetContextListener(PresetContextListener pl);

    public void addPresetListener(PresetListener pl, Integer[] presets);

    public void removePresetListener(PresetListener pl, Integer[] presets);

    // PRESET COLLECTION
    // returns Set of Integer
    public Set getPresetIndexesInContext() throws NoSuchContextException;

    // returns List of ContextReadablePreset/ReadablePreset (e.g FLASH/ROM samples returned as ReadablePreset)
    public List getContextPresets() throws NoSuchContextException;

    // returns List of ReadablePreset or better ( e.g FLASH/ROM and out of context samples returned as ReadablePreset)
    public List getDatabasePresets() throws NoSuchContextException;

    // set of integers
    public Set getDatabaseIndexes() throws NoSuchContextException;

    // returns Map of Integer -> String
    public Map getPresetNamesInContext() throws NoSuchContextException;

    public boolean isPresetInContext(Integer preset);

    public int size();

    public boolean isPresetEmpty(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void expandContext(PresetContext pc, Integer[] presets) throws NoSuchContextException, NoSuchPresetException;

    public List expandContextWithEmptyPresets(PresetContext pc, Integer reqd) throws NoSuchContextException;

    public int numEmpties(Integer[] presets) throws NoSuchPresetException, NoSuchContextException;

    public int numEmpties(Integer lowPreset, int num) throws NoSuchPresetException, NoSuchContextException;

    public List findEmptyPresetsInContext(Integer reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException;

    public Integer firstEmptyPresetInContext() throws NoSuchContextException, NoSuchPresetException;

    public Integer firstEmptyPresetInDatabaseRange(Integer lowPreset, Integer highPreset) throws NoSuchContextException, NoSuchPresetException;

    public PresetContext newContext(String name, Integer[] presets) throws NoSuchPresetException, NoSuchContextException;

    public void release() throws NoSuchContextException;


    /* public boolean presetExists(Integer preset);
     public boolean voiceExists(Integer preset, Integer voice);
     public boolean zoneExists(Integer preset, Integer voice, Integer zone);
     public boolean linkExists(Integer preset, Integer link);
      */

    // PRESET
    // value between 0 and 1 representing fraction of dump completed
    // value < 0 means no dump in progress
    public IntegerUseMap getSampleIndexesInUseForUserPresets() throws NoSuchContextException;

    public IntegerUseMap getSampleIndexesInUseForFlashPresets() throws NoSuchContextException;

    public IntegerUseMap getSampleIndexesInUseForAllPresets() throws NoSuchContextException;

    public double getInitializationStatus(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void newPreset(Integer preset, String name) throws NoSuchPresetException, NoSuchContextException;

    public void newPreset(IsolatedPreset ip, Integer preset, String name) throws NoSuchPresetException, NoSuchContextException;

   public void newPreset(IsolatedPreset ip, Integer preset, String name, Map sampleTranslationMap, Integer defaultSampleTranslation, Map linkTranslationMap) throws NoSuchPresetException, NoSuchContextException;

    // public void newPreset(IsolatedPreset[] isoPresets, Integer basePreset, boolean findEmpties) throws NoSuchPresetException, NoSuchContextException;

    public Map offsetLinkIndexes(Integer preset, Integer offset, boolean user) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException;

    public Map offsetSampleIndexes(Integer preset, Integer offset, boolean user) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException;

    public Map remapLinkIndexes(Integer preset, Map translationMap) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException;

    public Map remapSampleIndexes(Integer preset, Map translationMap, Integer defaultSampleTranslation) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException;

    public IsolatedPreset getIsolatedPreset(Integer preset) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException;

    public static final int PRESET_VOICES_SELECTOR = 0;
    public static final int PRESET_ZONES_SELECTOR = 1;
    public static final int PRESET_VOICES_AND_ZONES_SELECTOR = 2;

    public static final int MODE_APPLY_SAMPLE_TO_ALL_VOICES = PRESET_VOICES_SELECTOR;
    public static final int MODE_APPLY_SAMPLE_TO_ALL_ZONES = PRESET_ZONES_SELECTOR;
    public static final int MODE_APPLY_SAMPLE_TO_ALL_VOICES_AND_ZONES = PRESET_VOICES_AND_ZONES_SELECTOR;
    public static final int MODE_APPLY_SAMPLE_TO_NEW_VOICE = 3;

    public static final int MODE_APPLY_SAMPLES_TO_NEW_VOICES = 20;
    public static final int MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES = 21;

    public void applySampleToPreset(Integer preset, Integer sample, int mode) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException;

    public void applySamplesToPreset(Integer preset, Integer[] samples, int mode) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException, TooManyZonesException;

    public void lockPresetWrite(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void unlockPreset(Integer preset);

    public boolean isPresetInitialized(Integer preset) throws NoSuchPresetException;

    public void assertPresetInitialized(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void assertPresetRemote(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void assertPresetNamed(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public int getPresetState(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public String getPresetSummary(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public boolean isPresetWriteLocked(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public boolean isPresetWritable(Integer preset);

    public Set getPresetSet(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public String getPresetName(Integer preset) throws NoSuchPresetException, PresetEmptyException;

    public void setPresetName(Integer preset, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void copyPreset(Integer srcPreset, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void copyPreset(Integer srcPreset, Integer destPreset, Map presetLinkTranslationMap) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void copyPreset(Integer srcPreset, Integer destPreset, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void erasePreset(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void refreshPreset(Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public Integer[] getPresetParams(Integer preset, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchContextException;

    public Integer[] setPresetParams(Integer preset, Integer[] ids, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchContextException, ParameterValueOutOfRangeException;

    public void combineVoices(Integer preset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public Set getUsedGroupIndexes(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public Integer getNextAvailableGroup(Integer preset, boolean atTail) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void purgeZones(Integer preset) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException;

    public int numPresetZones(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public int numPresetSamples(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public Set presetSampleSet(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public com.pcmsolutions.util.IntegerUseMap presetSampleUsage(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public int numPresetLinkPresets(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public Set presetLinkPresetSet(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public com.pcmsolutions.util.IntegerUseMap presetLinkPresetUsage(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public ContextReadablePreset getContextPreset(Integer preset) throws NoSuchPresetException;

    public ReadablePreset getReadablePreset(Integer preset) throws NoSuchPresetException;

    public ContextEditablePreset getEditablePreset(Integer preset) throws NoSuchPresetException;

    public void sortVoices(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException;

    public void sortLinks(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException;

    public void sortZones(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException;

    // VOICE
    public void splitVoice(Integer preset, Integer voice, int splitKey) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException, TooManyVoicesException, ParameterValueOutOfRangeException, NoSuchVoiceException;

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer preset, Integer voice) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, NoSuchVoiceException;

    public Integer newVoice(Integer preset, IsolatedPreset.IsolatedVoice iv) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, TooManyVoicesException, TooManyZonesException;

    public Integer newVoices(Integer preset, Integer num, Integer[] sampleNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException, NoSuchContextException;

    public void rmvVoices(Integer preset, Integer[] voices) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException, CannotRemoveLastVoiceException;

    public void copyVoice(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException, NoSuchContextException;

    public int numVoices(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public Integer[] getGroupParams(Integer preset, Integer group, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, NoSuchContextException;

    public Integer[] getVoiceParams(Integer preset, Integer voice, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException;

    public Integer[] setGroupParamFromVoice(Integer preset, Integer voice, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException;

    public Integer[] setGroupParam(Integer preset, Integer group, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, NoSuchContextException, ParameterValueOutOfRangeException;

    public Integer[] setVoicesParam(Integer preset, Integer[] voices, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException;

    public void expandVoice(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException, TooManyVoicesException;

    public boolean trySetOriginalKeyFromName(Integer preset, Integer voice, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException;

    public boolean trySetOriginalKeyFromName(Integer preset, Integer voice, Integer zone, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchZoneException;

    public boolean trySetOriginalKeyFromSampleName(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, SampleEmptyException, NoSuchSampleException;

    public boolean trySetOriginalKeyFromSampleName(Integer preset, Integer voice, Integer zone) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchZoneException, SampleEmptyException, NoSuchSampleException;
    //public void getPresetPackHeader()

    //public void combineVoices(Integer preset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException, TooManyVoicesException;

    public void getVoiceMultiSample(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer destVoice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

    public void refreshVoiceParameters(Integer preset, Integer voice, Integer[] ids) throws NoSuchContextException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException, ParameterValueOutOfRangeException, IllegalParameterIdException;

    public Integer[] getVoiceIndexesInGroupFromVoice(Integer preset, Integer voice) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, NoSuchVoiceException;

    public Integer[] getVoiceIndexesInGroup(Integer preset, Integer group) throws NoSuchGroupException, PresetEmptyException, NoSuchContextException, NoSuchPresetException;

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer preset, Integer link) throws NoSuchLinkException, NoSuchPresetException, NoSuchContextException, PresetEmptyException;

    public Integer newLink(Integer preset, IsolatedPreset.IsolatedLink il) throws TooManyVoicesException, PresetEmptyException, NoSuchContextException, NoSuchPresetException;

    public Integer newLinks(Integer preset, Integer num, Integer[] presetNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException, NoSuchContextException;

    public void rmvLinks(Integer preset, Integer[] links) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, NoSuchContextException;

    public int numLinks(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void copyLink(Integer srcPreset, Integer srcLink, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException, NoSuchContextException;

    public Integer[] getLinkParams(Integer preset, Integer link, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, NoSuchContextException;

    public Integer[] setLinksParam(Integer preset, Integer[] links, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, NoSuchContextException, ParameterValueOutOfRangeException;

    // ZONE
    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer preset, Integer voice, Integer zone) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException;

    public Integer newZone(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, NoSuchVoiceException, TooManyZonesException;

    public Integer newZones(Integer preset, Integer voice, Integer num) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException, NoSuchContextException;

    public void rmvZones(Integer preset, Integer voice, Integer[] zones) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException;

    public int numZones(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException;

    public Integer[] getZoneParams(Integer preset, Integer voice, Integer zone, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException;

    public Integer[] setZonesParam(Integer preset, Integer voice, Integer[] zones, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException, ParameterValueOutOfRangeException;

    public void setDiversePresetParams(AbstractPresetParameterProfile[] paramProfiles) throws NoSuchPresetException, NoSuchContextException;

    public abstract interface AbstractPresetParameterProfile {
        public Integer getId();

        public Integer getValue();

        //public void setNewValues(PresetContext pc); // returns actual values after setting, some elements might be null if the target didn't exist

        //public Integer[] getSetIds();

        //public Integer[] getSetValues();
    }

    public interface PresetParameterProfile extends AbstractPresetParameterProfile {
        public Integer getPreset();
    }

    public interface VoiceParameterProfile extends PresetParameterProfile {
        public Integer getVoice();
    }

    public interface LinkParameterProfile extends PresetParameterProfile {
        public Integer getLink();
    }

    public interface GroupParameterProfile extends PresetParameterProfile {
        public Integer getGroup();
    }

    public interface ZoneParameterProfile extends VoiceParameterProfile {
        public Integer getZone();
    }
}
