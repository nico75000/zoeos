package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterPositionalReflectionZMTC extends AbstractParameterZMTCommand {
    public int getMinNumTargets() {
        return 2;
    }

    public boolean isSuitableInToolbar() {
        return false;
    }

    public String getPresentationString() {
        return "Reflect positionally";
    }

    public String getDescriptiveString() {
        return "Reflect values positionally about center value of selection";
    }

    public void execute(Object invoker, Object[] arguments) throws CommandFailedException, ZCommandTargetsNotSpecifiedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
    }

    public String getMenuPathString() {
        return ";Reflect";
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        EditableParameterModel[] params = getTargets().toArray(new EditableParameterModel[numTargets()]);
        try {
            ParameterModelUtilities.reflectPositionallyModels(params);
        } catch (Exception e) {
            throw new CommandFailedException(e.getMessage());
        }
        return false;
    }
}
