/*
 * PresetObject.java
 *
 * Created on February 9, 2003, 2:11 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author  pmeehan
 */
public interface ContextReadablePreset extends ReadablePreset {

    public Set getPresetIndexesInContext();

    public Map getPresetNamesInContext();

    public List findEmptyPresets(Integer reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException;

    public boolean presetEmpty(Integer preset) throws NoSuchPresetException;

    public void copyPreset(Integer destPreset) throws NoSuchPresetException, PresetEmptyException;

    public void copyPreset(Integer destPreset, String name) throws NoSuchPresetException, PresetEmptyException;

    public void copyVoice(Integer srcVoice, Integer destPreset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException;

    public void getVoiceMultiSample(Integer srcVoice, Integer destPreset, Integer destVoice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

    public void copyLink(Integer srcLink, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException;

    public PresetContext getPresetContext();
}
