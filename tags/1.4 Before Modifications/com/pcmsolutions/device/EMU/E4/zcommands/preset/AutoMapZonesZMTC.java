package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AutoMapZonesZMTC extends AbstractEditableZoneZMTCommand {
    public int getMinNumTargets() {
        return 2;
    }

    public String getPresentationString() {
        return "AutoMapZ";
    }

    public String getDescriptiveString() {
        return "Auto-map zones based on original key";
    }

    public String getMenuPathString() {
        return ";Key Mapping";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice.EditableZone editableZone, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice.EditableZone[] zones = getTargets().toArray(new ContextEditablePreset.EditableVoice.EditableZone[numTargets()]);
        PresetContextMacros.autoMapZoneKeyWin(zones);
        return false;
    }
}

