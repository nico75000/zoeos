package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.ZUtilities;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ErasePresetZMTC extends AbstractContextBasicEditablePresetZMTCommand {

    public int getMnemonic() {
        return KeyEvent.VK_E;
    }

    public String getPresentationString() {
        return "Erase";
    }

    public String getDescriptiveString() {
        return "Erase preset";
    }

    public boolean handleTarget(ContextBasicEditablePreset contextBasicEditablePreset, int total, int curr) throws Exception {
        ContextBasicEditablePreset[] presets = getTargets().toArray(new ContextBasicEditablePreset[numTargets()]);
        int num = presets.length;
        if (UserMessaging.askYesNo("Erase " + ZUtilities.quantify(presets.length, "preset") + "?"))
            for (int n = 0; n < num; n++)
                presets[n].erasePreset();
        return false;
    }
}
