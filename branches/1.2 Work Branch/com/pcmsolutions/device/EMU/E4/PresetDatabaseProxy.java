package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;

import java.util.Set;

interface PresetDatabaseProxy {

    public PresetEventHandler getPresetEventHandler();

    public void addPresetListener(PresetListener pl, Integer[] presets);

    public void removePresetListener(PresetListener pl, Integer[] presets);

    public PDBWriter getDBWrite();

    public PDBReader getDBRead();

    public Set getReadablePresets();

    public PresetContext getRootContext();

    public SampleContext getRootSampleContext();
}

