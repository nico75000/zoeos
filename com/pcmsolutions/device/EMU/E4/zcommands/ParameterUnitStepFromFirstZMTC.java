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
public class ParameterUnitStepFromFirstZMTC extends AbstractParameterZMTCommand {
    private double percentFact;
    private double unitStep;
    private double currStep;
    private boolean custom = false;
    private static int rng = 12;

    public ParameterUnitStepFromFirstZMTC() {
        this(rng);
    }

    private ParameterUnitStepFromFirstZMTC(double unitStep) {
        super(String.valueOf((int) unitStep) + " units", "Offset from first value's % using a unit step of " + String.valueOf(unitStep) + " units", null, null);
        this.unitStep = unitStep;
    }

    private ParameterUnitStepFromFirstZMTC(boolean custom) {
        super("Custom", "Offset from first value's % using a custom provided unit step ", new String[]{"Step in Units"}, new String[]{"Step in Units"});
        this.custom = true;
    }


    public int getMinNumTargets() {
        return 2;
    }

    public ZMTCommand getNextMode() {
        if (custom)
            return null;

        if (unitStep == -rng)
            return new ParameterUnitStepFromFirstZMTC(true);
        if (unitStep == 1)
            return new ParameterUnitStepFromFirstZMTC(unitStep - 2); // skip unit step of zero
        else
            return new ParameterUnitStepFromFirstZMTC(unitStep - 1);
    }

    public String getMenuPathString() {
        return ";Step from First";
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        if (custom)
            if (arguments[0] != null && !arguments[0].toString().equals(""))
                try {
                    return "Step by " + Double.parseDouble(arguments[0].toString()) + " units";
                } catch (NumberFormatException e) {
                    return "Not a valid number";
                }

        return super.getSummaryString(arguments);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (custom && index == 0)
            return new FixedLengthTextField("", 6);

        return null;
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
        if (num == 0)
            throw new CommandFailedException("Need at least 2 targets");

        int maxv = params[0].getParameterDescriptor().getMaxValue().intValue();
        int minv = params[0].getParameterDescriptor().getMinValue().intValue();
        int currv;
        try {
            currv = params[0].getValue().intValue();
            double rng = maxv - minv;
            if (rng == 0.0)
                percentFact = 1.0;
            else
                percentFact = ((currv - minv) / rng);
        } catch (ParameterUnavailableException e) {
            throw new CommandFailedException("First Parameter not available");
        }


        currStep = 0;
        ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(params), new EditableParameterModel.EditChainValueProvider() {
            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                Integer retVal = percentAndStepValue(model);
                currStep += unitStep;
                return retVal;
            }
        });
    }

    private Integer percentAndStepValue(EditableParameterModel p) {
        int maxv = p.getParameterDescriptor().getMaxValue().intValue();
        int minv = p.getParameterDescriptor().getMinValue().intValue();

        int newv = (int) Math.round(((minv + ((maxv - minv) * percentFact)) + currStep));

        if (newv < minv)
            newv = minv;
        else if (newv > maxv)
            newv = maxv;

        return IntPool.get(newv);
    }
}
