package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterDefaultZMTC extends AbstractParameterZMTCommand {

    public ParameterDefaultZMTC() {
        super("Default", "Set parameter to defualt value", null, null);
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
        if (num == 0) {
            // try use primary target
            p = getTarget();
            ParameterModelUtilities.defaultParameterModels(new Object[]{p});
        } else
            ParameterModelUtilities.defaultParameterModels(params);
    }
}
