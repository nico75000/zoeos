package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.PresetContext;

import java.io.ByteArrayInputStream;


interface PresetContextFactory {

    public PresetContext newPresetContext(String name, PresetDatabaseProxy pdbp);

    public Object initializePresetAtIndex(Integer index, PresetEventHandler peh);

    public boolean remoteInitializePresetAtIndex(Integer index, PresetEventHandler peh, ByteArrayInputStream is);

    public double getPresetInitializationStatus(Integer index);

    public String initializePresetNameAtIndex(Integer index, PresetEventHandler peh);

}

