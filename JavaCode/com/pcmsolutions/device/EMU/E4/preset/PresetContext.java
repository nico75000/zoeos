/*
 * PresetContext.java
 *
 * Created on January 3, 2003, 9:10 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.database.*;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.tasking.Ticket;
import com.pcmsolutions.util.IntegerUseMap;
import com.pcmsolutions.gui.ProgressCallback;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 *
 * @author  pmeehan
 */
public interface PresetContext extends Context<ReadablePreset, PresetContext, PresetListener, IsolatedPreset>{
    public static final Preferences prefs = Preferences.userNodeForPackage(PresetContext.class.getClass()).node("PresetContext");
    public static final String PREF_autoGroup = "autoGroup";
    public static final String PREF_autoGroupAtTail = "autoGroupAtTail";
    public static final String PREF_tryMatchAppliedSamples = "tryMatchAppliedSamples";
    public static final ZBoolPref ZPREF_tryMatchAppliedSamples = new Impl_ZBoolPref(prefs, PREF_tryMatchAppliedSamples, true);

    public DeviceParameterContext getDeviceParameterContext() throws DeviceException;

    public DeviceContext getDeviceContext();

    public SampleContext getRootSampleContext();

    // EVENTS
    // AUDITION
    public Ticket auditionSamples(Integer[] sample, boolean consecutive) ;

    public Ticket auditionPreset(Integer preset);

    public Ticket auditionVoices(Integer preset, Integer[] voices, boolean consecutive);

    // PRESET COLLECTION
    // returns Set of Integer
    public ContextEditablePreset[] getEditablePresets() throws DeviceException;

    // returns List of ContextReadablePreset/ReadablePreset (e.g FLASH/ROM samples returned as ReadablePreset)
    public List<ReadablePreset> getContextPresets() throws DeviceException;

    // returns List of ReadablePreset or better ( e.g FLASH/ROM and out of context samples returned as ReadablePreset)
    public List<ReadablePreset> getDatabasePresets() throws DeviceException;

    public double getInitializationStatus(Integer preset) throws  DeviceException;

    public Ticket newContent(Integer index, String name) ;

    // returns true if was originally empty but newed succesfully, false if not empty
    public boolean newIfEmpty(Integer index, String name) throws DeviceException, ContentUnavailableException;

    // if empty refreshes to check for content, if still empty a new is performed, returns true if forced content actually performed
    public boolean newIfReallyEmpty(Integer index, String name) throws DeviceException, ContentUnavailableException;

    public Ticket dropContent(IsolatedPreset ip, Integer preset, String name, ProgressCallback prog);

    public Ticket dropContent(IsolatedPreset ip, Integer preset, String name, Map sampleTranslationMap, Integer defaultSampleTranslation, Map linkTranslationMap, ProgressCallback prog);

    public Ticket offsetLinkIndexes(Integer preset, Integer offset, boolean user);

    public Ticket offsetSampleIndexes(Integer preset, Integer offset, boolean user) ;

    public Ticket remapLinkIndexes(Integer preset, Map translationMap);

    public Ticket remapSampleIndexes(Integer preset, Map translationMap, Integer defaultSampleTranslation);

    public Ticket refreshPresetSamples(Integer preset);

    public IsolatedPreset getIsolatedPreset(Integer preset, boolean refreshSamples) throws DeviceException, EmptyException, ContentUnavailableException;

    public static final int PRESET_VOICES_SELECTOR = 0;
    public static final int PRESET_ZONES_SELECTOR = 1;
    public static final int PRESET_VOICES_AND_ZONES_SELECTOR = 2;

    public static final int MODE_APPLY_SAMPLE_TO_ALL_VOICES = PRESET_VOICES_SELECTOR;
    public static final int MODE_APPLY_SAMPLE_TO_ALL_ZONES = PRESET_ZONES_SELECTOR;
    public static final int MODE_APPLY_SAMPLE_TO_ALL_VOICES_AND_ZONES = PRESET_VOICES_AND_ZONES_SELECTOR;
    public static final int MODE_APPLY_SAMPLE_TO_NEW_VOICE = 3;

    public static final int MODE_APPLY_SAMPLES_TO_NEW_VOICES = 20;
    public static final int MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES = 21;

    public Ticket applySampleToPreset(Integer preset, Integer sample, int mode) ;

    public Ticket applySamplesToPreset(Integer preset, Integer[] samples, int mode) ;

    public Ticket assertRemote(Integer preset) ;

    public String getPresetSummary(Integer preset) throws DeviceException;

    public Set getPresetsDeepSet(Integer[] presets) throws DeviceException, ContentUnavailableException;

    public Set<Integer> getPresetDeepSet(Integer preset) throws DeviceException, ContentUnavailableException;

    public Ticket copy(Integer srcPreset, Integer destPreset, Map presetLinkTranslationMap);

    public Integer[] getPresetParams(Integer preset, Integer[] ids) throws EmptyException, ParameterException, DeviceException, ContentUnavailableException;

    public Ticket setPresetParam(Integer preset, Integer id, Integer value) ;

    public Ticket offsetPresetParam(Integer preset, Integer id, Integer offset) ;

    public Ticket offsetPresetParam(Integer preset, Integer id, Double offsetAsFOR) throws IllegalParameterIdException;

    public Ticket combineVoices(Integer preset, Integer group) throws EmptyException, DeviceException, ContentUnavailableException;

    public Set getUsedGroupIndexes(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public Integer getNextAvailableGroup(Integer preset, boolean atTail) throws DeviceException, EmptyException, ContentUnavailableException;

    public Ticket purgeZones(Integer preset);

    public Ticket purgeLinks(Integer preset);

    public Ticket purgeEmpties(Integer preset);

    public int numPresetZones(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public int numPresetSamples(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public Set presetSampleSet(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public IntegerUseMap presetSampleUsage(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public int numPresetLinkPresets(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public Set presetLinkPresetSet(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public IntegerUseMap presetLinkPresetUsage(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public ContextReadablePreset getContextPreset(Integer preset) throws DeviceException;

    public ReadablePreset getReadablePreset(Integer preset) throws  DeviceException;

    public Ticket sortVoices(Integer preset, Integer[] ids);

    public Ticket sortLinks(Integer preset, Integer[] ids) ;

    public Ticket sortZones(Integer preset, Integer[] ids);

    // VOICE
    public Ticket splitVoice(Integer preset, Integer voice, int splitKey) ;

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer preset, Integer voice) throws DeviceException, EmptyException, ContentUnavailableException, PresetException;

    public Ticket newVoice(Integer preset, IsolatedPreset.IsolatedVoice iv) ;

    public Ticket newVoices(Integer preset, Integer[] sampleNums) ;

    public Ticket rmvVoices(Integer preset, Integer[] voices);

    public Ticket copyVoice(Integer srcPreset, Integer srcVoice, Integer destPreset) ;

    public int numVoices(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public Integer[] getGroupParams(Integer preset, Integer group, Integer[] ids) throws EmptyException, ParameterException, PresetException, DeviceException, ContentUnavailableException;

    public Integer[] getVoiceParams(Integer preset, Integer voice, Integer[] ids) throws EmptyException, ParameterException, PresetException, DeviceException, ContentUnavailableException;

    public Integer[] getVoicesParam(Integer preset, Integer[] voices, Integer id) throws EmptyException, ParameterException, PresetException, DeviceException, ContentUnavailableException;

    public Ticket setGroupParamFromVoice(Integer preset, Integer voice, Integer id, Integer value) ;

    public Ticket offsetGroupParamFromVoice(Integer preset, Integer voice, Integer id, Integer offset) ;

    public Ticket offsetGroupParamFromVoice(Integer preset, Integer voice, Integer id, Double offsetAsFOR) ;

    public Ticket setGroupParam(Integer preset, Integer group, Integer id, Integer value) ;

    public Ticket setVoiceParam(Integer preset, Integer voice, Integer id, Integer value) ;

    public Ticket offsetVoiceParam(Integer preset, Integer voice, Integer id, Integer offset);

    public Ticket offsetVoiceParam(Integer preset, Integer voice, Integer id, Double offsetAsFOR) throws IllegalParameterIdException;

    public Ticket expandVoice(Integer preset, Integer voice);

    public Ticket trySetOriginalKeyFromName(Integer preset, Integer voice, String name);

    public Ticket trySetOriginalKeyFromName(Integer preset, Integer voice, Integer zone, String name) ;

    public Ticket trySetOriginalKeyFromSampleName(Integer preset, Integer voice) ;

    public Ticket trySetOriginalKeyFromSampleName(Integer preset, Integer voice, Integer zone) ;

    public Ticket refreshVoiceParameters(Integer preset, Integer voice, Integer[] ids) ;

    public Integer[] getVoiceIndexesInGroupFromVoice(Integer preset, Integer voice) throws EmptyException, DeviceException, PresetException, ContentUnavailableException;

    public Integer[] getVoiceIndexesInGroup(Integer preset, Integer group) throws PresetException, EmptyException, DeviceException, ContentUnavailableException;

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer preset, Integer link) throws PresetException, DeviceException, EmptyException, ContentUnavailableException;

    public Ticket newLink(Integer preset, IsolatedPreset.IsolatedLink il);

    public Ticket newLinks(Integer preset, Integer[] presetNums);

    public Ticket rmvLinks(Integer preset, Integer[] links) ;

    public int numLinks(Integer preset) throws EmptyException, DeviceException, ContentUnavailableException;

    public Ticket copyLink(Integer srcPreset, Integer srcLink, Integer destPreset) ;

    public Integer[] getLinkParams(Integer preset, Integer link, Integer[] ids) throws EmptyException, ParameterException, PresetException, DeviceException, ContentUnavailableException;

    public Ticket setLinkParam(Integer preset, Integer link, Integer id, Integer value);

    public Ticket offsetLinkParam(Integer preset, Integer link, Integer id, Integer offset);

    public Ticket offsetLinkParam(Integer preset, Integer link, Integer id, Double offsetAsFOR) throws IllegalParameterIdException;

    // ZONE
    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer preset, Integer voice, Integer zone) throws DeviceException, PresetException, ContentUnavailableException, EmptyException;

    public Ticket newZone(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) ;

    public Ticket newZones(Integer preset, Integer voice, Integer num) ;

    public Ticket rmvZones(Integer preset, Integer voice, Integer[] zones) ;

    public int numZones(Integer preset, Integer voice) throws EmptyException, PresetException, DeviceException, ContentUnavailableException;

    public Integer[] getZoneParams(Integer preset, Integer voice, Integer zone, Integer[] ids) throws EmptyException, ParameterException, DeviceException, PresetException,  ContentUnavailableException;

    public Ticket setZoneParam(Integer preset, Integer voice, Integer zone, Integer id, Integer value) ;

    public Ticket offsetZoneParam(final Integer preset, final Integer voice, final Integer zone, final Integer id, final Integer offset);

    public Ticket offsetZoneParam(final Integer preset, final Integer voice, final Integer zone, final Integer id, final Double offsetAsFOR) throws IllegalParameterIdException;
}
