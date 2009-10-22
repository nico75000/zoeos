package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.CommandFailedException;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AssertRemotePresetZMTC extends AbstractContextEditablePresetZMTCommand {

    public int getMnemonic() {
        return KeyEvent.VK_A;
    }

    public String getPresentationString() {
        return "Assert remote";
    }

    public String getDescriptiveString() {
        return "Send locally cached preset to remote";
    }

    public String getMenuPathString() {
        return ";Utility";
    }

    public boolean handleTarget(ContextEditablePreset p, int total, int curr) throws Exception {
        if (!p.isInitialized())
            throw new CommandFailedException(p.getDisplayName() + " is not initialized");
        p.assertRemote();
        return true;
    }
}

