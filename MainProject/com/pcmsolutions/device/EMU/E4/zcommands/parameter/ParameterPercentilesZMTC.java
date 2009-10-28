package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.system.ZMTCommand;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterPercentilesZMTC extends AbstractParameterZMTCommand {
    private final int percent;

    public ParameterPercentilesZMTC() {
        this(0);
    }

    private ParameterPercentilesZMTC(int percent) {
        this.percent = percent;
    }

    public ZMTCommand getNextMode() {
        if (percent == 100)
            return null;
        return new ParameterPercentilesZMTC(percent + 5);
    }

    public boolean isSuitableInToolbar() {
        return false;
    }

    public String getPresentationString() {
        return String.valueOf(percent) + "%";
    }

    public String getDescriptiveString() {
        return "Set to " + String.valueOf(percent) + "% of range";
    }

    public String getMenuPathString() {
        if (percent % 10 == 0)
            return ";Set;10 percentiles";
        else
            return ";Set;5 percentiles";
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        model.setValue(ParameterModelUtilities.calcFOR(model, percent / 100.0));
        return true;
    }
}
