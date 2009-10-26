package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class MaxParameterZMTC extends AbstractParameterZMTCommand {

    public MaxParameterZMTC() {
        super("Maximize", "Set parameter to maximum value", null, null);
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
                maxValue(p);
            }
            for (int n = 0; n < num; n++) {
                maxValue(params[n]);
                Thread.yield();
            }
        } catch (ParameterValueOutOfRangeException e) {
            throw new CommandFailedException("Parameter Value Out Of Range");
        } catch (IllegalParameterIdException e) {
            throw new CommandFailedException("Illegal Parameter");
        } catch (ParameterUnavailableException e) {
            throw new CommandFailedException("Parameter unavailable");
        }
    }

    private void maxValue(EditableParameterModel p) throws ParameterValueOutOfRangeException, ParameterUnavailableException, CommandFailedException, IllegalParameterIdException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        p.setValue(p.getParameterDescriptor().getMaxValue());
    }
}
