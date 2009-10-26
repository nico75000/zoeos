package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterLinearFadeZMTC extends AbstractParameterZMTCommand {
    private double startPercent;
    private double endPercent;
    private boolean inclusive = false;
    private int cmdMode;

    public ParameterLinearFadeZMTC() {
        this(0, 100, false, 0);
    }

    public int getMinNumTargets() {
        return 2;
    }

    private ParameterLinearFadeZMTC(double startPercent, double endPercent, boolean inclusive, int cmdMode) {
        super(String.valueOf((int) startPercent) + "% .. " + String.valueOf((int) endPercent) + "% " + (inclusive ? "(incl)" : "(excl)"), "Linear fade values from " + String.valueOf((int) startPercent) + "% to " + String.valueOf((int) endPercent) + "% " + (inclusive ? "(incl)" : "(excl)"), null, null);
        this.startPercent = startPercent;
        this.endPercent = endPercent;
        this.inclusive = inclusive;
        this.cmdMode = cmdMode;
    }

    private ParameterLinearFadeZMTC(boolean inclusive, int cmdMode) {
        super("First value's % .. last value's % " + (inclusive ? "(incl)" : "(excl)"), "Linear fade values from " + "first value's % to last value's %" + (inclusive ? " (incl)" : " (excl)"), null, null);
        this.inclusive = inclusive;
        this.cmdMode = cmdMode;
    }

    public ZMTCommand getNextMode() {
        ZMTCommand zc = null;
        if (cmdMode == 0)
            zc = new ParameterLinearFadeZMTC(0, 100, true, 1);
        else if (cmdMode == 1)
            zc = new ParameterLinearFadeZMTC(100, 0, false, 2);
        else if (cmdMode == 2)
            zc = new ParameterLinearFadeZMTC(100, 0, true, 3);
        else if (cmdMode == 3)
            zc = new ParameterLinearFadeZMTC(false, 4);
        else if (cmdMode == 4)
            zc = new ParameterLinearFadeZMTC(true, 5);

        return zc;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public String getMenuPathString() {
        return ";Linear Fade";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        EditableParameterModel[] params = getTargets();
        int num = params.length;

        if (num < 2)
            throw new CommandFailedException("Need at least 2 targets for a linear fade");

        final double[] percents = new double[num];

        if (cmdMode == 4 || cmdMode == 5) {
            try {
                int maxFirst = params[0].getParameterDescriptor().getMaxValue().intValue();
                int minFirst = params[0].getParameterDescriptor().getMinValue().intValue();
                int firstVal = params[0].getValue().intValue();

                int maxLast = params[num - 1].getParameterDescriptor().getMaxValue().intValue();
                int minLast = params[num - 1].getParameterDescriptor().getMinValue().intValue();
                int lastVal = params[num - 1].getValue().intValue();

                startPercent = (double) (firstVal - minFirst) / (double) (maxFirst - minFirst) * 100.0;
                endPercent = (double) (lastVal - minLast) / (double) (maxLast - minLast) * 100.0;

            } catch (ParameterUnavailableException e) {
                throw new CommandFailedException("Parameter Unavailable");
            }
        }

        if (inclusive) {
            for (int i = 0; i < num; i++)
                percents[i] = startPercent + ((endPercent - startPercent) / (num - 1)) * i;
        } else {
            for (int i = 0; i < num; i++)
                percents[i] = startPercent + ((endPercent - startPercent) / num) * i;
        }

        ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(params), new EditableParameterModel.EditChainValueProvider() {
            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                return ParameterModelUtilities.calcPOR(model, percents[valIndex++]);
            }
        });
    }

    private int valIndex = 0;
}
