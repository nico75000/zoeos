/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;


/**
 *
 * @author  pmeehan
 */

public interface ContextBasicEditablePreset extends ContextReadablePreset {

    public ContextReadablePreset getContextReadablePresetDowngrade();

    // PRESET
    public void setPresetName(String name) throws NoSuchPresetException, PresetEmptyException;

    public void lockPresetWrite() throws NoSuchPresetException, NoSuchContextException;

    public void erasePreset() throws NoSuchPresetException, PresetEmptyException;
}
