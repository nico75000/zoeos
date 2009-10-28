package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.JumpDownIcon;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.JumpUpIcon;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterJumpZMTC extends AbstractParameterZMTCommand {
    private boolean up;

    public ParameterJumpZMTC() {
        init(false);
    }

    void init(boolean up) {
        this.up = up;
    }

    public boolean isSuitableAsButton() {
        return true;
    }

    public String getPresentationString() {
        return (up ? "Jump up" : "Jump down");
    }

    public String getDescriptiveString() {
        return (up ? "Jump up (+5%)" : "Jump down (-5%)");
    }

    public ZMTCommand getNextMode() {
        if (up)
            return null;
        ParameterJumpZMTC next = new ParameterJumpZMTC();
        next.init(true);
        return next;
    }

    public Icon getIcon() {
        if (up)
            return JumpUpIcon.INSTANCE;
        else
            return JumpDownIcon.INSTANCE;
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        if (up)
            ParameterModelUtilities.jumpUp(getTargets().toArray(new EditableParameterModel[numTargets()]));
        else
            ParameterModelUtilities.jumpDown(getTargets().toArray(new EditableParameterModel[numTargets()]));
        return false;
    }
}
