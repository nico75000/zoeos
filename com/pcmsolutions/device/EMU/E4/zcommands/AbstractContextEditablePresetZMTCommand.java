package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextEditablePresetZMTCommand extends AbstractZMTCommand implements E4ContextEditablePresetZCommandMarker {
    protected AbstractContextEditablePresetZMTCommand() {
        super(ContextEditablePreset.class);
    }

    protected AbstractContextEditablePresetZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextEditablePreset.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextEditablePreset getTarget() {
        return (ContextEditablePreset) target;
    }

    public ContextEditablePreset[] getTargets() {
        if (targets == null)
            return new ContextEditablePreset[0];

        int num = targets.length;
        ContextEditablePreset[] presets = new ContextEditablePreset[num];

        for (int n = 0; n < num; n++) {
            presets[n] = (ContextEditablePreset) targets[n];
        }
        return presets;
    }
}
