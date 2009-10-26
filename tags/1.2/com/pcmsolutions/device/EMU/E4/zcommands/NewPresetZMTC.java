package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class NewPresetZMTC extends AbstractContextEditablePresetZMTCommand {

    public NewPresetZMTC() {
        super("Preset", "Create New Preset", null, null);
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
        ContextEditablePreset[] presets = getTargets();
        int num = presets.length;
        ContextEditablePreset p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                if (p == null)
                    throw new CommandFailedException("Null Target");
                newPreset(p);
            }
            for (int n = 0; n < num; n++) {
                newPreset(presets[n]);
                Thread.yield();
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        }
    }

    private void newPreset(ContextEditablePreset p) throws CommandFailedException, NoSuchPresetException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        p.newPreset(p.getPresetNumber(), DeviceContext.UNTITLED_PRESET);
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public String getMenuPathString() {
        return ";New";
    }

    public Icon getIcon() {
        return null;
    }
}
