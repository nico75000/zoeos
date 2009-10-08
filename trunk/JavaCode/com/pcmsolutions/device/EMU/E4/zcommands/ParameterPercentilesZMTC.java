package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommand;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterPercentilesZMTC extends AbstractParameterZMTCommand {
    private int percent;

    public ParameterPercentilesZMTC() {
        this(0);
    }

    private ParameterPercentilesZMTC(int percent) {
        super(String.valueOf(percent) + "%", "Set value(s) to " + String.valueOf(percent) + "% of range", null, null);
        this.percent = percent;
    }

    public ZMTCommand getNextMode() {
        if (percent == 100)
            return null;
        return new ParameterPercentilesZMTC(percent + 5);
    }

    public String getMenuPathString() {
        if (percent % 10 == 0)
            return ";Set;10 Percentiles";
        else
            return ";Set;5 Percentiles";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        EditableParameterModel[] params = getTargets();
        int num = params.length;
        EditableParameterModel p;
        if (num == 0) {
            // try use primary target
            p = getTarget();
            ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(new Object[]{p}), new EditableParameterModel.EditChainValueProvider() {
                public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                    return ParameterModelUtilities.calcFOR(model, percent / 100.0);
                }
            });
        } else
            ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(params), new EditableParameterModel.EditChainValueProvider() {
                public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                    return ParameterModelUtilities.calcFOR(model, percent / 100.0);
                }
            });
    }
}
