package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;

import java.awt.event.KeyEvent;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class RefreshPresetZMTC extends AbstractReadablePresetZMTCommand {

    public int getMnemonic() {
        return KeyEvent.VK_R;
    }

    public String getPresentationString() {
        return "Refresh";
    }

    public String getDescriptiveString() {
        return "Refresh preset from remote";
    }

    public boolean handleTarget(ReadablePreset readablePreset, int total, int curr) throws Exception {
        ReadablePreset[] presets = getTargets().toArray(new ReadablePreset[numTargets()]);
        int num = presets.length;
        HashSet done = new HashSet();
        for (int n = 0; n < num; n++) {
            if (!done.contains(presets[n])) {
                presets[n].refresh();
                done.add(presets[n]);
            }
        }
        return false;
    }
}

