package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.gui.UserMessaging;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class MatchOriginalKeysToSampleNameZMTC extends AbstractContextEditablePresetZMTCommand {
    public String getPresentationString() {
        return "Match keys";
    }

    public String getDescriptiveString() {
        return "Try match original keys to sample names";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
        int mode;
        int zc = 0;
        for (int i = 0; i < presets.length; i++)
            zc += PresetContextMacros.presetZoneCount(presets[i].getPresetContext(), presets[i].getIndex());

        if (zc != 0) {
            int res = UserMessaging.askOptions("Original key matching", "Apply Matching to", new String[]{"Voices", "Zones", "Voices and Zones"});
            if (res == JOptionPane.CLOSED_OPTION)
                return false;
            if (res == 0)
                mode = PresetContext.PRESET_VOICES_SELECTOR;
            else if (res == 1)
                mode = PresetContext.PRESET_ZONES_SELECTOR;
            else
                mode = PresetContext.PRESET_VOICES_AND_ZONES_SELECTOR;
        } else
            mode = PresetContext.PRESET_VOICES_SELECTOR;

        for (int i = 0; i < presets.length; i++)
            PresetContextMacros.trySetOriginalKeyFromSampleName(presets[i].getPresetContext(), new Integer[]{presets[i].getIndex()}, mode);
        return false;
    }

    public int getMinNumTargets() {
        return 1;
    }
}

