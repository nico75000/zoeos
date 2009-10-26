package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
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
public class DefaultPresetViewerZMTC extends AbstractReadablePresetZMTCommand {
    private int problemCount;
    private int emptyCount;

    public DefaultPresetViewerZMTC() {
        super("View", "Default View of Preset", null, null);
    }

    public int getMnemonic() {
        return KeyEvent.VK_W;
    }


    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
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
                if (p.getPresetState() == RemoteObjectStates.STATE_EMPTY) {
                    problemCount++;
                    emptyCount++;
                } else
                    p.getDeviceContext().getViewManager().openPreset(p);
            } else {
                HashMap done = new HashMap();
                for (int n = 0; n < num; n++) {
                    if (!done.containsKey(presets[n])) {
                        // if (presets[n].getPresetState() == RemoteObjectStates.STATE_EMPTY) {
                        //     problemCount++;
                        //     emptyCount++;
                        //  } else {
                        presets[n].getDeviceContext().getViewManager().openPreset(presets[n]);
                        // viewPreset(presets[n]);
                        done.put(presets[n], null);
                        // }
                    }
                    Thread.yield();
                }
                if (problemCount == targets.length && emptyCount == targets.length) {
                    throw new CommandFailedException((targets.length == 1 ? "Unable to create view - preset was empty" : "Unable to create views - all the presets were empty"));
                } else if (problemCount == targets.length && emptyCount == 0) {
                    throw new CommandFailedException((targets.length == 1 ? "Unable to create view" : "Unable to create views"));
                } else if (problemCount > 0 && emptyCount == 0)
                    throw new CommandFailedException("Unable to create view for some of the targets");
                else if (problemCount > 0 && problemCount == emptyCount)
                    throw new CommandFailedException("Unable to create view for some of the targets - some presets were empty");
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        }
    }


    public String getMenuPathString() {
        //return ";Views";
        return "";
    }
}

