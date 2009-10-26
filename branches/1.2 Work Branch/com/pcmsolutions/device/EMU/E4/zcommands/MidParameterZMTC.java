package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchLinkException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class MidParameterZMTC extends AbstractParameterZMTCommand {

    public MidParameterZMTC() {
        super("Middle Value", "Set parameter2 to middle value", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        EditableParameterModel[] params = getTargets();
        int num = params.length;
        EditableParameterModel p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                if (p == null)
                    throw new CommandFailedException("Null Target");
                midValue(p);
            }
            for (int n = 0; n < num; n++) {
                midValue(params[n]);
                Thread.yield();
            }
        } catch (Exception e) {
            throw new CommandFailedException(e.getMessage());
        }
    }

    private void midValue(EditableParameterModel p) throws ParameterValueOutOfRangeException, ParameterUnavailableException, CommandFailedException, IllegalParameterIdException, NoSuchPresetException, NoSuchLinkException, NoSuchContextException, PresetEmptyException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        int min = p.getParameterDescriptor().getMinValue().intValue();
        int max = p.getParameterDescriptor().getMaxValue().intValue();
        p.setValue(IntPool.get((int) (min + (max - min) * 0.5)));
    }

}
