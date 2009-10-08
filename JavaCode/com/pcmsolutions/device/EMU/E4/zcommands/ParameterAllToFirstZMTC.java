package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommand;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterAllToFirstZMTC extends AbstractParameterZMTCommand {
    public ParameterAllToFirstZMTC() {
        super("Repeat First", "Set all selected values using the first value's percentage in range", null, null);
    }

    public ZMTCommand getNextMode() {
        return null;
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getMenuPathString() {
        return "";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        EditableParameterModel[] params = getTargets();
        ParameterModelUtilities.repeatParameterModels(params);
    }
}
