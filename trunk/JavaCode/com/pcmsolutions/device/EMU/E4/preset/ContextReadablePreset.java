/*
 * PresetObject.java
 *
 * Created on February 9, 2003, 2:11 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextReadablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;


/**
 *
 * @author  pmeehan       
 */
public interface ContextReadablePreset extends ReadablePreset {

    final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextReadablePresetZCommandMarker.class, ReadablePreset.cmdProviderHelper);

    public SortedSet<Integer> getPresetIndexesInContext() throws PresetException;

    public Map<Integer,String> getPresetNamesInContext() throws PresetException;

    public SortedSet<Integer> findEmptyPresets(Integer reqd, Integer beginIndex, Integer maxIndex) throws PresetException;

    public void copyPreset(Integer destPreset) throws  PresetException;

    public void copyPreset(Integer destPreset, String name) throws PresetException;

    public void copyVoice(Integer srcVoice, Integer destPreset) throws PresetException;

    public void copyLink(Integer srcLink, Integer destPreset) throws PresetException;

    public PresetContext getPresetContext();
}
