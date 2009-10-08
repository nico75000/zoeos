package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextBasicEditablePresetZMTCommand extends AbstractZMTCommand implements E4ContextBasicEditablePresetZCommandMarker {
    protected AbstractContextBasicEditablePresetZMTCommand() {
        super(ContextBasicEditablePreset.class);
    }

    protected AbstractContextBasicEditablePresetZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextBasicEditablePreset.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextEditablePreset getTarget() {
        return (ContextEditablePreset) target;
    }

    public ContextBasicEditablePreset[] getTargets() {
        if (targets == null)
            return new ContextBasicEditablePreset[0];

        int num = targets.length;
        ContextBasicEditablePreset[] presets = new ContextBasicEditablePreset[num];

        for (int n = 0; n < num; n++) {
            presets[n] = (ContextBasicEditablePreset) targets[n];
        }
        return presets;
    }
}
