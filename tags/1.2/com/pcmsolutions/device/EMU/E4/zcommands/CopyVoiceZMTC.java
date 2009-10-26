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
public class CopyVoiceZMTC extends AbstractEditableVoiceZMTCommand {

    public CopyVoiceZMTC() {
        super("Copy", "Copy Voice", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
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
                v.copyVoice();
            } else {
                //Arrays.sort(voiceIndexes);
                for (int n = 0; n < num; n++) {
                    voices[n].copyVoice();
                    Thread.yield();
                }
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        } catch (NoSuchVoiceException e) {
            throw new CommandFailedException("No such voice ");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (CannotRemoveLastVoiceException e) {
            throw new CommandFailedException("Cannot remove last voice");
        } catch (TooManyVoicesException e) {
            throw new CommandFailedException("Too many voices");
        }
    }
}

