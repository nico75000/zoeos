package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AutoMapPresetZMTC extends AbstractContextEditablePresetZMTCommand {
    public AutoMapPresetZMTC() {
        super("AutoMap", "AutoMap all voices and zones in the preset", null, null);
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
        ContextEditablePreset[] presets = getTargets();
        for (int i = 0; i < presets.length; i++) {
            try {
                int nv = presets[i].numVoices();
                PresetContextMacros.autoMapVoiceKeyWin(presets[i].getPresetContext(), presets[i].getPresetNumber(), ZUtilities.fillIncrementally(new Integer[nv], 0));
            } catch (NoSuchPresetException e) {
                throw new CommandFailedException(e.getMessage());
            } catch (PresetEmptyException e) {
            } catch (NoSuchContextException e) {
                throw new CommandFailedException(e.getMessage());
            }
        }
    }
}

