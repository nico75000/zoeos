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
public class DeleteVoiceZMTC extends AbstractEditableVoiceZMTCommand {

    public DeleteVoiceZMTC() {
        super("Delete", "Delete Voice", null, null);
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
                v.removeVoice();
            } else {
                Arrays.sort(voices);

                boolean allSamePresetAndContext = true;
                for (int n = 1; n < voices.length; n++) {
                    if (!(voices[n].getPresetContext().equals(voices[0].getPresetContext()) && voices[n].getPresetNumber().equals(voices[0].getPresetNumber()))) {
                        allSamePresetAndContext = false;
                        break;
                    }
                }
                if (allSamePresetAndContext) {
                    Integer[] voiceIndexes = new Integer[voices.length];
                    for (int i = 0, n = voices.length; i < n; i++)
                        voiceIndexes[i] = voices[i].getVoiceNumber();
                    voices[0].getPresetContext().rmvVoices(voices[0].getPresetNumber(), voiceIndexes);
                } else
                    for (int n = num - 1; n >= 0; n--) {
                        if (n % 4 == 0)
                           // try {
                          //      Thread.sleep(100);
                          //  } catch (InterruptedException e) {
                          //      e.printStackTrace();
                          //  }
                        voices[n].removeVoice();
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
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("No such context");
        }
    }
}

