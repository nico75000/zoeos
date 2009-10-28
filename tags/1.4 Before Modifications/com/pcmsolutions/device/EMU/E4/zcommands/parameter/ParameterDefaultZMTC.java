package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.DefaultIcon;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterDefaultZMTC extends AbstractParameterZMTCommand {

    public String getPresentationString() {
        return "Default";
    }

    public String getDescriptiveString() {
        return "Set to default";
    }

    public Icon getIcon() {
        return DefaultIcon.INSTANCE;
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        ParameterModelUtilities.defaultParameterModels(getTargets().toArray());
        return false;
    }
}
