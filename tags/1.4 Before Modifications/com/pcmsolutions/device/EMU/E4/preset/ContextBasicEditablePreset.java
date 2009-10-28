/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextBasicEditablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;


/**
 *
 * @author  pmeehan
 */

public interface ContextBasicEditablePreset extends ContextReadablePreset {
    public final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextBasicEditablePresetZCommandMarker.class, ContextReadablePreset.cmdProviderHelper);

    public ContextReadablePreset getContextReadablePresetDowngrade();

    // PRESET
    public void setPresetName(String name) throws  PresetException;

    public void erasePreset() throws  PresetException;
}
