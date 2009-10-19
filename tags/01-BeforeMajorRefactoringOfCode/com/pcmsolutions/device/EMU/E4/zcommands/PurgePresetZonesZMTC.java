package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
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
public class PurgePresetZonesZMTC extends AbstractContextEditablePresetZMTCommand {

    public PurgePresetZonesZMTC() {
        super("Purge Zones", "Purge all sample zones in preset", null, null);
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
                purgeZones(p);
            }
            for (int n = 0; n < num; n++) {
                purgeZones(presets[n]);
                Thread.yield();
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        }
    }

    private void purgeZones(ContextEditablePreset p) throws CommandFailedException, NoSuchPresetException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        try {
            p.purgeZones();
        } catch (PresetEmptyException e) {
        }
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public String getMenuPathString() {
        return ";Utility";
    }
}
