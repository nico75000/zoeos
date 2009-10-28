package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.callback.Callback;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AuditionPresetZMTC extends AbstractReadablePresetZMTCommand {

    public int getMnemonic() {
        return KeyEvent.VK_A;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public boolean isSuitableAsButton() {
        return true;
    }

    public String getPresentationString() {
        return "Audition";
    }

    public String getDescriptiveString() {
        return "Audition preset on audition midi channel";
    }

    public boolean handleTarget(ReadablePreset readablePreset, int total, int curr) throws Exception {
        readablePreset.audition().post(new Callback() {
            public void result(Exception e, boolean wasCancelled) {
                if (e != null && !wasCancelled)
                    UserMessaging.showCommandFailed(e.getMessage());
            }
        });
        return true;
    }
}

