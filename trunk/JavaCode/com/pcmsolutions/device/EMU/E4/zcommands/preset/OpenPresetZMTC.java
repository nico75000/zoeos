package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.callback.Callback;

import java.awt.event.KeyEvent;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class OpenPresetZMTC extends AbstractReadablePresetZMTCommand {
    boolean exclusive;

    public OpenPresetZMTC() {
        this(false);
    }

    public OpenPresetZMTC(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public ZMTCommand getNextMode() {
        if (!exclusive)
            return new OpenPresetZMTC(true);
        else
            return null;
    }

    public int getMnemonic() {
        return (exclusive ? 0 : KeyEvent.VK_O);
    }

    public String getPresentationString() {
        return (exclusive ? "Open [exclusive]":"Open");
    }

    public String getDescriptiveString() {
        return (exclusive ? "Open preset exclusively by clearing workspace first":"Open the preset");
    }

    public boolean handleTarget(ReadablePreset p, int total, int curr) throws Exception {
        final ReadablePreset[] presets = getTargets().toArray(new ReadablePreset[numTargets()]);
        final int num = presets.length;
        final HashSet done = new HashSet();
        if (exclusive)
            p.getDeviceContext().getViewManager().clearDeviceWorkspace().post(new Callback() {
                public void result(Exception e, boolean wasCancelled) {
                    for (int n = 0; n < num; n++) {
                        if (!done.contains(presets[n])) {
                            presets[n].performOpenAction(n == 0);
                            done.add(presets[n]);
                        }
                    }
                }
            });
        else
            for (int n = 0; n < num; n++) {
                if (!done.contains(presets[n])) {
                    presets[n].performOpenAction(n == 0);
                    done.add(presets[n]);
                }
            }
        return false;
    }
}

