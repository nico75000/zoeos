package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterUnitStepFromCurrentZMTC extends AbstractParameterZMTCommand {
    private double unitStep;
    private boolean custom = false;
    private static int rng = 12;

    public ParameterUnitStepFromCurrentZMTC() {
        this(rng);
    }

    private ParameterUnitStepFromCurrentZMTC(double unitStep) {
        if (unitStep == 1) {
            init("Increment", "Offset value by 1 unit", null, null);
        } else if (unitStep == -1) {
            init("Decrement", "Offset value by -1 units", null, null);
        } else {
            init(String.valueOf((int) unitStep) + " units", "Offset value(s) by " + String.valueOf((int) unitStep) + " units", null, null);
        }
        this.unitStep = unitStep;
    }

    private ParameterUnitStepFromCurrentZMTC(boolean custom) {
        init("Custom", "Offset value(s) by custom number of units", new String[]{"Offset in Units"}, new String[]{"Offset in Units"});
        this.custom = true;
    }

    public int getMinNumTargets() {
        return 1;
    }

    public ZMTCommand getNextMode() {
        if (custom)
            return null;

        if (unitStep == -rng)
            return new ParameterUnitStepFromCurrentZMTC(true);
        if (unitStep == 1)
            return new ParameterUnitStepFromCurrentZMTC(unitStep - 2); // skip unit step of zero
        else
            return new ParameterUnitStepFromCurrentZMTC(unitStep - 1);
    }

    public String getMenuPathString() {
        if (unitStep == 1 || unitStep == -1)
            return "";
        return ";Offset";
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (custom && index == 0)
            return new FixedLengthTextField("", 6);

        return null;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        if (custom)
            if (arguments[0] != null && !arguments[0].toString().equals(""))
                try {
                    return "Offset by " + Double.parseDouble(arguments[0].toString()) + " units";
                } catch (NumberFormatException e) {
                    return "Not a valid number";
                }

        return super.getSummaryString(arguments);
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (custom) {
            try {
                unitStep = Double.parseDouble(arguments[0].toString());
            } catch (NumberFormatException e) {
                throw new CommandFailedException("Not a valid number");
            }
        }

        EditableParameterModel[] params = getTargets();
        int num = params.length;
        EditableParameterModel p;

        if (num == 0) {
            // try use primary target
            p = getTarget();
            ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(new Object[]{p}), new EditableParameterModel.EditChainValueProvider() {
                public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                    return stepValue(model);
                }
            });
        } else
            ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(params), new EditableParameterModel.EditChainValueProvider() {
                public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                    return stepValue(model);
                }
            });
    }

    private Integer stepValue(EditableParameterModel p) throws ParameterUnavailableException {
        int maxv = p.getParameterDescriptor().getMaxValue().intValue();
        int minv = p.getParameterDescriptor().getMinValue().intValue();

        int currv = p.getValue().intValue();

        int newv = (int) Math.round(currv + unitStep);

        if (newv < minv)
            newv = minv;
        else if (newv > maxv)
            newv = maxv;

        return IntPool.get(newv);
    }
}
