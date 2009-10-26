package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.Content;
import com.pcmsolutions.util.IntegerUseMap;

import java.util.Map;
import java.util.Set;

/**
 * User: paulmeehan
 * Date: 24-Mar-2004
 * Time: 12:23:29
 */
public interface DatabasePreset extends DatabaseParameterized, Content {
    void setGroupValue(Integer group, Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException;

    void offsetGroupValue(Integer group, Integer id, Integer offset, boolean constrain) throws ParameterValueOutOfRangeException, IllegalParameterIdException;

    String getNotes();

    void setNotes(String notes);

    DatabaseVoice[] getVoicesInGroup(Integer group);

    void applySamples(Integer[] samples, int mode) throws TooManyVoicesException, TooManyZonesException, ParameterValueOutOfRangeException;

    void applySingleSample(Integer sample, int mode) throws ParameterValueOutOfRangeException, TooManyVoicesException;

    void combineVoices(Integer group);

    void expandVoice(final Integer voice) throws TooManyVoicesException;

    void refreshParameters(Integer[] ids) throws IllegalParameterIdException;

    // returned map format is voice(Integer)->new sample value(Integer)
    Map offsetSampleIndexes(Integer sampleOffset, boolean user);

    // returned map format is link(Integer)->new link value(Integer)
    Map offsetLinkIndexes(Integer linkOffset, boolean user);

    Integer splitVoice(Integer voice, int key) throws NoSuchVoiceException, TooManyVoicesException, ParameterValueOutOfRangeException;

    // unmatched rom sample indexes should not appear in translationMap
    Map remapSampleIndexes(Map translationMap, Integer defaultSampleTranslation);

    Map remapLinkIndexes(Map translationMap);

    void sortZones(Integer[] ids);

    void sortVoices(Integer[] ids);

    void sortLinks(Integer[] ids);

    DatabaseVoice getLeadVoiceInGroup(Integer group);

    Integer[] getVoiceIndexesInGroup(Integer group);

    Integer getLeadVoiceIndexInGroup(Integer group);

    public String getSummary();
    
    String getName();

    void setName(String name);

    int numZones();

    void purgeLinks();

    int numReferencedSamples();

    Set referencedSampleSet();

    int numReferencedPresets();

    IntegerUseMap referencedPresetUsage();

    IntegerUseMap referencedSampleUsage();

    Set<Integer> referencedPresetSet();

    IsolatedPreset getIsolated();

    IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer v) throws NoSuchVoiceException;

    IsolatedPreset.IsolatedLink getIsolatedLink(Integer l) throws NoSuchLinkException;

    DatabaseVoice getVoice(Integer v) throws NoSuchVoiceException;

    DatabaseLink getLink(Integer l) throws NoSuchLinkException;

    Integer newVoices(Integer[] sampleNumbers) throws TooManyVoicesException;

    Integer copyDatabaseVoice(DatabaseVoice voice) throws TooManyVoicesException;

    Integer dropVoice(IsolatedPreset.IsolatedVoice iv) throws TooManyVoicesException, TooManyZonesException;

    void rmvVoices(Integer[] voices) throws NoSuchVoiceException, CannotRemoveLastVoiceException;

    Integer newLinks(Integer[] presetNumbers) throws TooManyVoicesException;

    Integer copyDatabaseLink(DatabaseLink newLink) throws TooManyVoicesException;

    Integer dropLink(IsolatedPreset.IsolatedLink il) throws TooManyVoicesException;

    void rmvLink(Integer link) throws NoSuchLinkException;

    // IsolatedPreset stuff
    int numVoices();

    int numLinks();
}
