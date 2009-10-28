package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.RandomizeIcon;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import java.util.Iterator;
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

    private final int mode;
    private final double percent;
    private final int units;
    private final String absPath = "";
    private final String unitPath = ";Random;Units about current";
    private final String percentPath = ";Random;Percent of range about current";
    private final String percentOfCurrentPath = ";Random;Percent of current about current";

    public ParameterRandomizeZMTC() {
        mode = MODE_ABSOLUTE;
        percent = 0;
        this.units = 0;
    }

    private ParameterRandomizeZMTC(double percent, boolean ofCurrent) {
        this.mode = (ofCurrent ? MODE_RANGE_PERCENTAGE_OF_CURRENT : MODE_RANGE_PERCENTAGE);
        this.percent = percent;
        this.units = 0;
    }

    private ParameterRandomizeZMTC(int units) {
        this.mode = MODE_RANGE_UNITS;
        this.units = units;
        percent = 0;
    }

    public String getPresentationString() {
        if (mode == MODE_ABSOLUTE)
            return "Random";
        else
            return (units == 0 ? String.valueOf((int) percent) + " %" : String.valueOf(units) + " units");
    }

    public String getDescriptiveString() {
        if (mode == MODE_ABSOLUTE)
            return "Absolute randomize";
        else
            return (units == 0 ? "Randomize " + String.valueOf((int) percent) + "% about current" : "Randomize " + String.valueOf(units) + " units about current");
    }

    public Icon getIcon() {
        if (mode == MODE_ABSOLUTE)
            return RandomizeIcon.INSTANCE;
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

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        for (Iterator<EditableParameterModel> i = getTargets().iterator(); i.hasNext();)
            randomizeValue(i.next());
        return false;
    }

    private static final Random rand = new Random(Zoeos.getZoeosTicks());

    private void randomizeValue(EditableParameterModel p) throws ParameterException {
        int min = p.getParameterDescriptor().getMinValue().intValue();
        int max = p.getParameterDescriptor().getMaxValue().intValue();
        double randFact = java.lang.Math.abs((double) rand.nextInt() / (double) Integer.MAX_VALUE);
        double iUnits;
        switch (mode) {
            case MODE_RANGE_PERCENTAGE_OF_CURRENT:
                iUnits = ((p.getValue().intValue() - min) * percent / 100.0);
                p.offsetValue(calcOffset(iUnits * randFact));
                break;
            case MODE_RANGE_PERCENTAGE:
                iUnits = ((max - min) * percent / 100.0);
                p.offsetValue(calcOffset(iUnits * randFact));
                break;
            case MODE_RANGE_UNITS:
                p.offsetValue(calcOffset(units * randFact));
                break;
            case MODE_ABSOLUTE:
            default:
                p.setValue(IntPool.get(min + (int) Math.round(((max - min) * randFact))));
                break;
        }
    }

    Integer calcOffset(Double d) {
        return IntPool.get((int) Math.round(d - d / 2.0));
    }
}
