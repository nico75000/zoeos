package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextReadablePresetZMTCommand extends AbstractZMTCommand implements E4ContextReadablePresetZCommandMarker {
    protected AbstractContextReadablePresetZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextReadablePreset.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextReadablePreset getTarget() {
        return (ContextReadablePreset) target;
    }

    public ContextReadablePreset[] getTargets() {
        if (targets == null)
            throw new IllegalArgumentException("No targets specified");

        int num = targets.length;
        ContextReadablePreset[] presets = new ContextReadablePreset[num];

        for (int n = 0; n < num; n++) {
            presets[n] = (ContextReadablePreset) targets[n];
        }
        return presets;
    }
}
