package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AutoMapVoiceGroupZMTC extends AbstractEditableVoiceZMTCommand {

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        super.setTargets(targets);
        Integer grp;
        try {
            grp = getTargets()[0].getVoiceParams(new Integer[]{ID.group})[0];
            init("AutoMap G" + grp, "AutoMap voices in group based on original key", null, null);
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
        throw new ZMTCommandTargetsNotSuitableException();
    }

    public ZMTCommand getNextMode() {
        return null;
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset.EditableVoice[] voices = getTargets();
        try {
            PresetContextMacros.autoMapGroupKeyWin(voices[0].getPresetContext(), voices[0].getPresetNumber(), voices[0].getVoiceParams(new Integer[]{ID.group})[0]);
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (IllegalParameterIdException e) {
            throw new CommandFailedException("Internal error");
        } catch (NoSuchVoiceException e) {
            throw new CommandFailedException("No such voice");
        } catch (NoSuchGroupException e) {
            throw new CommandFailedException("No such group");
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("No such context");
        }
    }
}

