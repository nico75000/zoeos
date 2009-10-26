package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractReadablePresetZMTCommand extends AbstractZMTCommand implements E4ReadablePresetZCommandMarker {
    protected AbstractReadablePresetZMTCommand() {
        super(ReadablePreset.class);
    }

    protected AbstractReadablePresetZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ReadablePreset.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ReadablePreset getTarget() {
        return (ReadablePreset) target;
    }

    public ReadablePreset[] getTargets() {
        if (targets == null)
            return new ReadablePreset[0];

        int num = targets.length;
        ReadablePreset[] presets = new ReadablePreset[num];

        for (int n = 0; n < num; n++) {
            presets[n] = (ReadablePreset) targets[n];
        }
        return presets;
    }
}
