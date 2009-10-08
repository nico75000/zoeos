package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.CommandFailedException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AutoMapVoicesZMTC extends AbstractEditableVoiceZMTCommand {
    public AutoMapVoicesZMTC() {
        init("AutoMap", "AutoMap voices based on original key", null, null);
    }

    public int getMinNumTargets() {
        return 2;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset.EditableVoice[] voices = getTargets();
        try {
            PresetContextMacros.autoMapVoiceKeyWin(voices);
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        }
    }
}

