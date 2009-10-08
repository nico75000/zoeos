package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterRandomizeZMTC extends AbstractParameterZMTCommand {
    private final static int MODE_ABSOLUTE = 0;
    private final static int MODE_RANGE_UNITS = 1;
    private final static int MODE_RANGE_PERCENTAGE = 2;
    private final static int MODE_RANGE_PERCENTAGE_OF_CURRENT = 3;

    private int mode = MODE_ABSOLUTE;
    private double percent;
    private int units;
    private String absPath = ";Randomize";
    private String unitPath = ";Randomize;Units About Current";
    private String percentPath = ";Randomize;Percent of Range About Current";
    private String percentOfCurrentPath = ";Randomize;Percent of Current About Current";

    public ParameterRandomizeZMTC() {
        super("Absolute", "Set parameter2 to random value", null, null);
    }

    private ParameterRandomizeZMTC(double percent, boolean ofCurrent) {
        super(String.valueOf((int) percent) + " %", "Randomize parameter2 " + String.valueOf(percent) + "% about current", null, null);
        if (ofCurrent)
            this.mode = MODE_RANGE_PERCENTAGE_OF_CURRENT;
        else
            this.mode = MODE_RANGE_PERCENTAGE;
        this.percent = percent;
    }

    private ParameterRandomizeZMTC(int units) {
        super(String.valueOf(units) + " units", "Randomize parameter2 " + String.valueOf(units) + "units about current", null, null);
        this.mode = MODE_RANGE_UNITS;
        this.units = units;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public ZMTCommand getNextMode() {
        switch (mode) {
            case MODE_ABSOLUTE:
                return new ParameterRandomizeZMTC(1);
            case MODE_RANGE_UNITS:
                if (units == 16)
                    return new ParameterRandomizeZMTC((double) 5, false);
                else
                    return new ParameterRandomizeZMTC(units + 1);
            case MODE_RANGE_PERCENTAGE:
                if (percent == 95)
                    return new ParameterRandomizeZMTC((double) 5, true);
                else
                    return new ParameterRandomizeZMTC(percent + 5, false);
            case MODE_RANGE_PERCENTAGE_OF_CURRENT:
                if (percent == 95)
                    return null;
                else
                    return new ParameterRandomizeZMTC(percent + 5, true);
            default:
                return null;
        }
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        EditableParameterModel[] params = getTargets();
        int num = params.length;
        EditableParameterModel p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                if (p == null)
                    throw new CommandFailedException("Null Target");
                randomizeValue(p);
            } else
                ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(params), new EditableParameterModel.EditChainValueProvider() {
                    public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                        return randomizeValue(model);
                    }
                });
        } catch (ParameterUnavailableException e) {
            throw new CommandFailedException("Parameter unavailable");
        }
    }

    public String getMenuPathString() {
        switch (mode) {
            case MODE_RANGE_PERCENTAGE:
                return percentPath;
            case MODE_RANGE_PERCENTAGE_OF_CURRENT:
                return percentOfCurrentPath;
            case MODE_RANGE_UNITS:
                return unitPath;
            case MODE_ABSOLUTE:
            default:
                return absPath;
        }
    }

    private Integer randomizeValue(EditableParameterModel p) throws ParameterUnavailableException {
        java.util.Random rand = new Random(Zoeos.getZoeosTicks() * p.getParameterDescriptor().getId().intValue());
        int min = p.getParameterDescriptor().getMinValue().intValue();
        int max = p.getParameterDescriptor().getMaxValue().intValue();
        int curr = p.getValue().intValue();

        Integer newValue;
        double randFact = java.lang.Math.abs((double) rand.nextInt() / (double) Integer.MAX_VALUE);
        int off;
        switch (mode) {
            case MODE_RANGE_PERCENTAGE_OF_CURRENT:
            case MODE_RANGE_PERCENTAGE:
                if (mode == MODE_RANGE_PERCENTAGE_OF_CURRENT)
                    units = (int) ((curr - min) * percent / 100.0);
                else
                    units = (int) ((max - min) * percent / 100.0);
            case MODE_RANGE_UNITS:
                off = (int) Math.round(units * 2 * randFact);
                off -= units;
                if (curr + off > max)
                    newValue = IntPool.get(max);
                else if (curr + off < min)
                    newValue = IntPool.get(min);
                else
                    newValue = IntPool.get(curr + off);
                break;
            case MODE_ABSOLUTE:
            default:
                off = (int) Math.round(((max - min) * randFact));
                newValue = IntPool.get(min + off);
                break;
        }
        return newValue;
    }
}
