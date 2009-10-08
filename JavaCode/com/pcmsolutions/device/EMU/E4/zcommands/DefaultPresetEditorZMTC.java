package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
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
public class DefaultPresetEditorZMTC extends AbstractContextEditablePresetZMTCommand {
    private int problemCount;
    private int emptyCount;

    public DefaultPresetEditorZMTC() {
        super("Edit", "Default Editor for Preset", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public int getMnemonic() {
        return KeyEvent.VK_D;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset[] presets = getTargets();
        int num = presets.length;
        ContextEditablePreset p;
        if (num == 0) {
            // try use primary target
            p = getTarget();
            p.getDeviceContext().getViewManager().openPreset(p);
        } else {
            HashMap done = new HashMap();
            for (int n = 0; n < num; n++) {
                if (!done.containsKey(presets[n])) {
                    //   if (presets[n].getPresetState() == RemoteObjectStates.STATE_EMPTY) {
                    //     problemCount++;
                    //   emptyCount++;
                    //  } else {
                    presets[n].getDeviceContext().getViewManager().openPreset(presets[n]);
                    done.put(presets[n], null);
                    //  }
                }
                Thread.yield();
            }
            if (problemCount == targets.length && emptyCount == targets.length) {
                throw new CommandFailedException((targets.length == 1 ? "Unable to create editor - preset was empty" : "Unable to create editors - all the presets were empty"));
            } else if (problemCount == targets.length && emptyCount == 0) {
                throw new CommandFailedException((targets.length == 1 ? "Unable to create editor" : "Unable to create editors"));
            } else if (problemCount > 0 && emptyCount == 0)
                throw new CommandFailedException("Unable to create editor for some of the targets");
            else if (problemCount > 0 && problemCount == emptyCount)
                throw new CommandFailedException("Unable to create editor for some of the targets - some presets were empty");
        }
    }


    public String getMenuPathString() {
        //return ";Editors";
        return "";
    }
}

