package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.MinimizeIcon;
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
public class ParameterMinimizeZMTC extends AbstractParameterZMTCommand {

    public String getPresentationString() {
        return "Minimize";
    }

    public String getDescriptiveString() {
        return "Set to minimum";
    }

    public Icon getIcon() {
        return MinimizeIcon.INSTANCE;
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        model.setValue(model.getParameterDescriptor().getMinValue());
        return true;
    }
}
