/*
 * PresetListener.java
 *
 * Created on January 15, 2003, 10:55 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;


/**
 *
 * @author  pmeehan
 */
public interface PresetContextListener {
    public void presetsRemovedFromContext(PresetContext pc, Integer[] presets);

    public void presetsAddedToContext(PresetContext pc, Integer[] presets);

    public void contextReleased(PresetContext pc);
}
