package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.RotateLeftIcon;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.RotateRightIcon;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;

public class ParameterRotateZMTC extends AbstractParameterZMTCommand {
    final boolean left;

    public ParameterRotateZMTC(boolean left) {
        this.left = left;
    }

    public ParameterRotateZMTC() {
        this(true);
    }

    public ZMTCommand getNextMode() {
        if (left)
            return new ParameterRotateZMTC(false);
        return null;
    }

    public int getMinNumTargets() {
        return 2;
    }

    public boolean isSuitableAsButton() {
        return true;
    }

    public String getPresentationString() {
        return (left ? "RotateL" : "RotateR");
    }

    public String getDescriptiveString() {
        return (left ? "Rotate left (up)" : "Rotate right (down)");
    }

    public Icon getIcon() {
        if (left)
            return RotateLeftIcon.INSTANCE;
        return RotateRightIcon.INSTANCE;
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        Double[] rotatedFORs = ParameterModelUtilities.getRotatedFORs(getTargets().toArray(new ReadableParameterModel[numTargets()]), (left ? -1 : 1));
        int index = 0;
        for (EditableParameterModel m : getTargets())
            ParameterModelUtilities.applyFORToModel(m, rotatedFORs[index++]);
        return false;
    }
}
