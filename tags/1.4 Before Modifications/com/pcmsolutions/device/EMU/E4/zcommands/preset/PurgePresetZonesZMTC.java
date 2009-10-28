package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class PurgePresetZonesZMTC extends AbstractContextEditablePresetZMTCommand {

    public String getPresentationString() {
        return "Zones";
    }

    public String getDescriptiveString() {
        return "Purge all sample zones in preset";
    }

    public String getMenuPathString() {
        return ";Purge";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
        int num = presets.length;
        for (int n = 0; n < num; n++)
            if (!presets[n].isEmpty()) presets[n].purgeZones();
        return false;
    }
}
