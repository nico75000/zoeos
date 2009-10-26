package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class MatchOriginalKeysToSampleNameZMTC extends AbstractContextEditablePresetZMTCommand {
    public MatchOriginalKeysToSampleNameZMTC() {
        super("Try match original keys", "Try match original keys to sample names", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public int getMinNumTargets() {
        return 1;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset[] presets = getTargets();
        int num = presets.length;
        ContextEditablePreset p;
        int mode;
        int zc = 0;
        for (int i = 0; i < presets.length; i++)
            try {
                zc += PresetContextMacros.presetZoneCount(presets[i].getPresetContext(), presets[i].getPresetNumber());
            } catch (Exception e) {
                throw new CommandFailedException(e.getMessage());
            }

        if (zc != 0) {
            int res = UserMessaging.askOptions("Original key matching", "Apply Matching to", new String[]{"Voices", "Zones", "Voices and Zones"});
            if (res == JOptionPane.CLOSED_OPTION)
                return;
            if (res == 0)
                mode = PresetContext.PRESET_VOICES_SELECTOR;
            else if (res == 1)
                mode = PresetContext.PRESET_ZONES_SELECTOR;
            else
                mode = PresetContext.PRESET_VOICES_AND_ZONES_SELECTOR;
        } else
            mode = PresetContext.PRESET_VOICES_SELECTOR;

        if (num == 0) {
            // try use primary target
            p = getTarget();
            PresetContextMacros.trySetOriginalKeyFromSampleName(p.getPresetContext(), new Integer[]{p.getPresetNumber()}, mode);
        } else {
            for (int i = 0; i < presets.length; i++)
                PresetContextMacros.trySetOriginalKeyFromSampleName(presets[i].getPresetContext(), new Integer[]{presets[i].getPresetNumber()}, mode);
        }

    }
}

