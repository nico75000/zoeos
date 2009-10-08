package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ErasePresetZMTC extends AbstractContextBasicEditablePresetZMTCommand {

    public ErasePresetZMTC() {
        super("Erase", "Erase Preset", null, null);
    }

    public int getMnemonic() {
        return KeyEvent.VK_E;
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
        ContextBasicEditablePreset[] presets = getTargets();
        int num = presets.length;
        ContextBasicEditablePreset p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                if (p == null)
                    throw new CommandFailedException("Null Target");
                erasePreset(p);
            } else
                for (int n = 0; n < num; n++) {
                    erasePreset(presets[n]);
                    Thread.yield();
                }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        }
    }

    private void erasePreset(ContextBasicEditablePreset p) throws CommandFailedException, NoSuchPresetException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        try {
            p.erasePreset();
        } catch (PresetEmptyException e) {
        }
    }

    // if returns null no verification of command required
    public String getVerificationString() {
        int len = getTargets().length;
        return "Erase " + (len > 1 ? len + " preset locations?" : "preset location?");
    }

    public Icon getIcon() {
        return null;
        //return new ImageIcon("toolbarButtonGraphics/general/delete16.gif");
    }
}
