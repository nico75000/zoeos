package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class RemoveZoneZMTC extends AbstractEditableZoneZMTCommand {

    public RemoveZoneZMTC() {
        super("Delete", "Delete Zone", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset.EditableVoice.EditableZone[] zones = getTargets();
        int num = zones.length;
        ContextEditablePreset.EditableVoice.EditableZone z;

        try {
            if (num == 0) {
                // try use primary target
                z = getTarget();
                z.removeZone();
            } else {
                Arrays.sort(zones);
                for (int n = num - 1; n >= 0; n--) {
                    zones[n].removeZone();
                }

                Thread.yield();
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        } catch (NoSuchZoneException e) {
            throw new CommandFailedException("No such zone ");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (NoSuchVoiceException e) {
            throw new CommandFailedException("No such voice ");
        }
    }
}

