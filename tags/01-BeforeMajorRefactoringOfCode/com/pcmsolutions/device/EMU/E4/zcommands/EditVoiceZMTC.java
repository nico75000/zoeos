package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class EditVoiceZMTC extends AbstractEditableVoiceZMTCommand {
    protected boolean editAsCollection;

    public EditVoiceZMTC() {
        super("Open", "Open for editing", null, null);
        editAsCollection = false;
    }

    protected EditVoiceZMTC(boolean editAsCollection) {
        super("Open collection", "Open voice collection for editing", null, null);

        this.editAsCollection = true;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        super.setTargets(targets);
    }

    public ZMTCommand getNextMode() {
        if (targets.length < 2 || editAsCollection)
            return null;

        return new EditVoiceZMTC(true);
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
                editVoices(new ContextEditablePreset.EditableVoice[]{v});
            } else {
                editVoices(voices);
            }
        } catch (IllegalParameterIdException e) {
            throw new CommandFailedException("Problem setting up view");
        } catch (ComponentGenerationException e) {
            throw new CommandFailedException(e.getMessage());
        }
    }

    protected void editVoices(ContextEditablePreset.EditableVoice[] voices) throws IllegalParameterIdException, ComponentGenerationException {
        boolean tabbed = voices[0].getPreset().getDeviceContext().getDevicePreferences().ZPREF_useTabbedVoicePanel.getValue();
        boolean grouped = voices[0].getPreset().getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue();

        if (editAsCollection) {
            if (tabbed)
                voices[0].getPreset().getDeviceContext().getViewManager().openTabbedVoices(voices, grouped, true);
            else
                voices[0].getPreset().getDeviceContext().getViewManager().openVoices(voices, true);
        } else
            for (int i = 0; i < voices.length; i++)
                if (tabbed)
                    voices[i].getPreset().getDeviceContext().getViewManager().openTabbedVoice(voices[i], grouped, (i == 0 ? true : false));
                else
                    voices[i].getPreset().getDeviceContext().getViewManager().openVoice(voices[i], (i == 0 ? true : false));
    }
}


