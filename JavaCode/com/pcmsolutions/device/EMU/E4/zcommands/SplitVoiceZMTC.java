package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.NoteUtilities;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class SplitVoiceZMTC extends AbstractEditableVoiceZMTCommand {

    public SplitVoiceZMTC() {
        super("Split", "Split Voice on Key", new String[]{"note"}, new String[]{"note to split on"});
    }

    private static final Integer[] klh = new Integer[]{IntPool.get(45), IntPool.get(47)};

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        ContextEditablePreset.EditableVoice v = getTargets()[0];
        try {
            Integer[] vals = v.getVoiceParams(klh);
            int low = vals[0].intValue();
            int high = vals[1].intValue();
            if (low == high)
                throw new IllegalArgumentException("Can't split voice");

            NoteUtilities.Note[] notes = NoteUtilities.Note.getNoteRange(low + 1, high);
            return new JComboBox(notes);
        } catch (NoSuchPresetException e) {
        } catch (PresetEmptyException e) {
        } catch (IllegalParameterIdException e) {
        } catch (NoSuchVoiceException e) {
        }
        throw new IllegalArgumentException("unexpected error");
    }

    public int getMaxNumTargets() {
        return 1;
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
                v.splitVoice(((NoteUtilities.Note) arguments[0]).getNoteValue());
            } else {
                getTargets()[0].splitVoice(((NoteUtilities.Note) arguments[0]).getNoteValue());
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        } catch (NoSuchVoiceException e) {
            throw new CommandFailedException("No such voice ");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("No such context");
        } catch (TooManyVoicesException e) {
            throw new CommandFailedException("Too many voices");
        } catch (ParameterValueOutOfRangeException e) {
            throw new CommandFailedException("Illegal split key");
        }
    }
}

