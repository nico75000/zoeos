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
public abstract class AbstractEditableZoneZMTCommand extends AbstractZMTCommand implements E4EditableZoneZCommandMarker {
    protected AbstractEditableZoneZMTCommand() {
         super(ContextEditablePreset.EditableVoice.EditableZone.class);
     }

    protected AbstractEditableZoneZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextEditablePreset.EditableVoice.EditableZone.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextEditablePreset.EditableVoice.EditableZone getTarget() {
        return (ContextEditablePreset.EditableVoice.EditableZone) target;
    }

    public ContextEditablePreset.EditableVoice.EditableZone[] getTargets() {
        if (targets == null)
            return new ContextEditablePreset.EditableVoice.EditableZone[0];

        int num = targets.length;
        ContextEditablePreset.EditableVoice.EditableZone[] zones = new ContextEditablePreset.EditableVoice.EditableZone[num];

        for (int n = 0; n < num; n++) {
            zones[n] = (ContextEditablePreset.EditableVoice.EditableZone) targets[n];
        }
        return zones;
    }
}
