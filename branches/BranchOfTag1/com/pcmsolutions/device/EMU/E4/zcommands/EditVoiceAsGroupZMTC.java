package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchVoiceException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 26-Jul-2003
 * Time: 21:01:43
 * To change this template use Options | File Templates.
 */
public class EditVoiceAsGroupZMTC extends AbstractEditableVoiceZMTCommand {
    protected String group;

    public EditVoiceAsGroupZMTC() {
        super("Open G", "Open group for editing", null, null);

    }

    public int getMaxNumTargets() {
        return 1;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        super.setTargets(targets);
        try {
            group = getTargets()[0].getVoiceParams(new Integer[]{IntPool.get(37)})[0].toString();
            this.presString = "Open G" + group;
            return;
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        } catch (PresetEmptyException e) {
            e.printStackTrace();
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        } catch (NoSuchVoiceException e) {
            e.printStackTrace();
        }
        group = "";
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
        // should only be 1 voice in array!!
        ContextEditablePreset.EditableVoice gv = voices[0].duplicate();
        gv.setGroupMode(true);
        if (gv.getPreset().getDeviceContext().getDevicePreferences().ZPREF_useTabbedVoicePanel.getValue()) {
            gv.getPreset().getDeviceContext().getViewManager().openTabbedVoice(gv, gv.getPreset().getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue(), true);
        } else
            gv.getPreset().getDeviceContext().getViewManager().openVoice(gv, true);
    }
}
