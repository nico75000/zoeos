package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.system.ZUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AutoMapPresetZMTC extends AbstractContextEditablePresetZMTCommand {
    public String getPresentationString() {
        return "AutoMap";
    }

    public String getDescriptiveString() {
        return "Auto-map all voices and zones in the preset based on original key";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
        for (int i = 0; i < presets.length; i++) {
            int nv = presets[i].numVoices();
            PresetContextMacros.autoMapVoiceKeyWin(presets[i].getPresetContext(), presets[i].getIndex(), ZUtilities.fillIncrementally(new Integer[nv], 0));
        }
        return false;
    }

    public int getMinNumTargets() {
        return 1;
    }
}

