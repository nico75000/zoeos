package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class RemoveZoneZMTC extends AbstractEditableZoneZMTCommand {

    public String getPresentationString() {
        return "Delete";
    }

    public String getDescriptiveString() {
        return "Delete Zone";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice.EditableZone editableZone, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice.EditableZone[] zones = getTargets().toArray(new ContextEditablePreset.EditableVoice.EditableZone[numTargets()]);
        int num = zones.length;
        Arrays.sort(zones);
        for (int n = num - 1; n >= 0; n--)
            zones[n].removeZone();
        return false;
    }
}

