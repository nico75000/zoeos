package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;

import java.util.List;

interface PDBWriter extends PDBReader {

    public void releaseContext(PresetContext pc) throws NoSuchContextException;

    public void releaseContextPreset(PresetContext pc, Integer preset) throws NoSuchContextException, NoSuchPresetException;

    public PresetContext newContext(PresetContext pc, String name, Integer[] presets) throws NoSuchPresetException, NoSuchContextException;

    public void removePresetsFromContext(PresetContext src, Integer[] presets) throws NoSuchContextException, NoSuchPresetException;

    public void addPresetsToContext(PresetContext dest, Integer[] presets) throws NoSuchContextException, NoSuchPresetException;

    public List expandContextWithEmptyPresets(PresetContext src, PresetContext dest, Integer reqd) throws NoSuchContextException;

    public void release();

}

