package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.MaximizeIcon;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterMaximizeZMTC extends AbstractParameterZMTCommand {

    public String getPresentationString() {
        return "Maximize";
    }

    public String getDescriptiveString() {
        return "Set to maximum";
    }

    public Icon getIcon() {
        return MaximizeIcon.INSTANCE;
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        model.setValue(model.getParameterDescriptor().getMaxValue());
        return true;
    }
}
