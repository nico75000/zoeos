package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;

/**
 * User: paulmeehan
 * Date: 04-Mar-2004
 * Time: 13:10:41
 */
abstract public class AbstractPresetZCommand<T extends ReadablePreset> extends AbstractZCommand<T> {
    public String getPresentationCategory() {
        return "Preset";
    }
}
