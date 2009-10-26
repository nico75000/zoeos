package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class RefreshPresetZMTC extends AbstractReadablePresetZMTCommand {

    public RefreshPresetZMTC() {
        super("Refresh", "Refresh Presets From Remote", null, null);
    }

    public int getMnemonic() {
        return KeyEvent.VK_R;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public boolean isSuitableAsLaunchButton() {
        return true;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ReadablePreset[] presets = getTargets();
        int num = presets.length;
        ReadablePreset p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                refreshPreset(p);
            } else {
                HashMap done = new HashMap();
                for (int n = 0; n < num; n++) {
                    if (!done.containsKey(presets[n])) {
                        refreshPreset(presets[n]);
                        done.put(presets[n], null);
                    }
                    Thread.yield();
                }
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        }
    }

    private void refreshPreset(ReadablePreset p) throws CommandFailedException, NoSuchPresetException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        p.refreshPreset();
    }

    public Icon getIcon() {
        return null;
        //return new ImageIcon("toolbarButtonGraphics/general/refresh16.gif");
    }
}

