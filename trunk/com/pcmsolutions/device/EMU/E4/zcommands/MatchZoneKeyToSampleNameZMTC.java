package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class MatchZoneKeyToSampleNameZMTC extends AbstractEditableZoneZMTCommand {
    public MatchZoneKeyToSampleNameZMTC() {
        super("Try match original key", "Try match original key from sample name", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public int getMinNumTargets() {
        return 1;
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
                z.trySetOriginalKeyFromSampleName();
            } else {
                for ( int i=0;i<zones.length;i++)
                zones[i].trySetOriginalKeyFromSampleName();
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        } catch (NoSuchVoiceException e) {
            throw new CommandFailedException("No such voice ");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (NoSuchZoneException e) {
            throw new CommandFailedException("No Such Zone");
        }
    }
}

