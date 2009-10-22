package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.gui.UserMessaging;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class NewPresetZMTC extends AbstractContextEditablePresetZMTCommand {

    public String getPresentationString() {
        return "New";
    }

    public String getDescriptiveString() {
        return "Create a new preset";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
        int full = presets.length;
        for (ContextEditablePreset p : presets)
            if (p.getPresetContext().isReallyEmpty(p.getIndex()))
                full--;
        if (full > 0)
            if (!UserMessaging.askYesNo((full) + " preset" + (full > 1 ? "s" : "") + " will be overwritten. Continue with new?"))
                return false;
        for (ContextEditablePreset p : presets)
            p.newPreset(p.getIndex(), DeviceContext.UNTITLED_PRESET);
        return false;
    }
}
