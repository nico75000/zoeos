package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.RepeatIcon;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterRepeatZMTC extends AbstractParameterZMTCommand {
    final int mode;

    public ParameterRepeatZMTC() {
        this(0);
    }

    ParameterRepeatZMTC(int mode) {
        this.mode = mode;
    }

    public ZMTCommand getNextMode() {
        if (mode == 7)
            return null;
        else
            return new ParameterRepeatZMTC(mode + 1);
    }

    public int getMinNumTargets() {
        return 2 + mode;
    }

    public String getPresentationString() {
        return (mode == 0 ? "Repeat first" : "first " + String.valueOf(mode + 1));
    }

    public String getDescriptiveString() {
        return (mode == 0 ? "Repeat first throughout selection" : "Repeat first " + (mode + 1) + " throughout selection");
    }

    public Icon getIcon() {
        if (mode == 0)
            return RepeatIcon.INSTANCE;
        return null;
    }

    public String getMenuPathString() {
        if (mode == 0)
            return "";
        else
            return ";Repeat";
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        ParameterModelUtilities.repeatParameterModels(getTargets().toArray(), mode + 1);
        return false;
    }
}
