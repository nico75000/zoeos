package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.multimode.IllegalMidiChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.*;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class ReadablePresetToMultiModeZMTC extends AbstractReadablePresetZMTCommand {
    private int ch;
    private int maxCh = 16;
    private MultiModeContext mmc;

    public ReadablePresetToMultiModeZMTC() {
        this(1);
    }

    private ReadablePresetToMultiModeZMTC(int ch) {
        super("Ch " + String.valueOf(ch), "Send Preset(s) to Multimode " + "Ch " + String.valueOf(ch), null, null);
        this.ch = ch;
    }

    public ZMTCommand getNextMode() {
        if (ch >= maxCh)
            return null;

        return new ReadablePresetToMultiModeZMTC(ch + 1);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        super.setTargets(targets);
        try {
            if (targets.length > 0 && targets[0] != null) {
                mmc = ((ReadablePreset) targets[0]).getDeviceContext().getMultiModeContext();
                if (mmc.has32Channels())
                    maxCh = 32;
                else
                    maxCh = 16;
            }
            return;
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        }
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ReadablePreset[] presets = getTargets();
        int num = presets.length;
        if (num == 0) {
            try {
                getTarget().sendToMultiMode(IntPool.get(ch));
            } catch (IllegalMidiChannelException e) {
                e.printStackTrace();
            }
        } else {
            ReadablePreset p;
            for (int n = 0; n < num; n++) {
                int cch = ch + n;
                if (cch <= maxCh)
                    try {
                        presets[n].sendToMultiMode(IntPool.get(cch));
                    } catch (IllegalMidiChannelException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    public String getMenuPathString() {
        return ";Send To MultiMode";
    }
}

