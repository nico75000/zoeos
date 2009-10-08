package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchVoiceException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class MatchVoiceKeyToSampleNameZMTC extends AbstractEditableVoiceZMTCommand {
    public MatchVoiceKeyToSampleNameZMTC() {
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
        ContextEditablePreset.EditableVoice[] voices = getTargets();
        int num = voices.length;
        ContextEditablePreset.EditableVoice v;

        try {
            if (num == 0) {
                // try use primary target
                v = getTarget();
                v.trySetOriginalKeyFromSampleName();
            } else {
                for ( int i=0;i<voices.length;i++)
                voices[i].trySetOriginalKeyFromSampleName();
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        } catch (NoSuchVoiceException e) {
            throw new CommandFailedException("No such voice ");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        }
    }
}

