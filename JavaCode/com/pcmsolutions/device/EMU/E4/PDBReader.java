package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

interface PDBReader {

    public int getDBCount();

    public IntegerUseMap getSampleIndexesInUseForUserPresets(PresetContext pc) throws NoSuchContextException;

    public IntegerUseMap getSampleIndexesInUseForFlashPresets(PresetContext pc) throws NoSuchContextException;

    public IntegerUseMap getSampleIndexesInUseForAllPresets(PresetContext pc) throws NoSuchContextException;

    public List findEmptyPresets(PresetContext pc, Integer reqd) throws NoSuchContextException;

    public List findEmptyPresets(PresetContext pc, Integer reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException;

    public boolean seesPreset(PresetContext pc, Integer preset);

    public boolean readsPreset(PresetContext pc, Integer preset);

    public boolean hasPreset(PresetContext pc, Integer preset);

    public boolean isPresetInitialized(Integer preset) throws NoSuchPresetException;

    public PresetContext getRootContext();

    public String getPresetName(Integer preset) throws NoSuchPresetException;

    public void assertPresetNamed(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public String getPresetNameExtended(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void setPresetName(PresetContext pc, Integer preset, String name) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException;

    public void refreshPreset(PresetContext pc, Integer preset) throws NoSuchContextException, NoSuchPresetException;

    public Set getPresetIndexesInContext(PresetContext pc) throws NoSuchContextException;

    public Set getReadablePresetIndexes(PresetContext pc) throws NoSuchContextException;

    public Map getPresetNamesInContext(PresetContext pc) throws NoSuchContextException;

    public void changePresetObject(PresetContext pc, Integer preset, Object pobj) throws NoSuchPresetException, NoSuchContextException;

    public void lockPresetRead(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public boolean tryLockPresetRead(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public boolean tryLockPresetWrite(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void lockPresetWrite(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public PresetObject[] getPresetRW(PresetContext pc, Integer readPreset, Integer writePreset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    // Object[0] guaranteed to be a PresetObject
    // Object[1] could be either a PresetObject or EmptyPreset
    public Object[] getPresetRC(PresetContext pc, Integer readPreset, Integer writePreset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public void unlockPreset(Integer preset);

    public PresetObject getPresetRead(PresetContext pc, Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public PresetObject getPresetWrite(PresetContext pc, Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException;

    public String tryGetPresetSummary(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    // can return either a PresetObject or EmptyPreset
    public Object getPresetObjectWrite(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public int getPresetState(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public double getInitializationStatus(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public boolean remoteInitializePresetAtIndex(Integer index, ByteArrayInputStream is);

    public boolean isPresetWriteLocked(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException;

    public void release();

}

