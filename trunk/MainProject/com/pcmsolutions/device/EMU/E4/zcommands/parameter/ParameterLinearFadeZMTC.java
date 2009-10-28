package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.system.ZMTCommand;

public class ParameterLinearFadeZMTC extends AbstractParameterZMTCommand {
    private final double startPercent;
    private final double endPercent;
    private final boolean inclusive;
    private final int cmdMode;

    public ParameterLinearFadeZMTC() {
        this(0, 100, false, 0);
    }

    public int getMinNumTargets() {
        return 2;
    }

    private ParameterLinearFadeZMTC(double startPercent, double endPercent, boolean inclusive, int cmdMode) {
        this.inclusive = inclusive;
        this.cmdMode = cmdMode;
        this.startPercent = startPercent;
        this.endPercent = endPercent;
    }

    private ParameterLinearFadeZMTC(boolean inclusive, int cmdMode) {
        this.inclusive = inclusive;
        this.cmdMode = cmdMode;
        this.startPercent = 0;
        this.endPercent = 0;
    }

    public ZMTCommand getNextMode() {
        ZMTCommand zc;
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
        else
            zc = null;
        return zc;
    }

    public String getPresentationString() {
        if (cmdMode < 4)
            return (startPercent == 0 ? "Min" : "Max") + " .. " + (endPercent == 0 ? "min " : "max ") + (inclusive ? " (incl)" : " (excl)");
        else
            return "First .. last " + (inclusive ? "  (incl)" : "  (excl)");
    }

    public String getDescriptiveString() {
        if (cmdMode < 4)
            return "Linear fade of values from " + String.valueOf((int) startPercent) + "% to " + String.valueOf((int) endPercent) + "% " + (inclusive ? "  (incl)" : "  (excl)");
        else
            return "Linear fade from " + "first value to last value" + (inclusive ? "  (incl)" : "  (excl)");
    }

    public String getMenuPathString() {
        return ";Fade";
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        EditableParameterModel[] params = getTargets().toArray(new EditableParameterModel[numTargets()]);
        int num = params.length;

        final double[] percents = new double[num];
        double sp = startPercent;
        double ep = endPercent;

        if (cmdMode == 4 || cmdMode == 5) {
            int maxFirst = params[0].getParameterDescriptor().getMaxValue().intValue();
            int minFirst = params[0].getParameterDescriptor().getMinValue().intValue();
            int firstVal = params[0].getValue().intValue();

            int maxLast = params[num - 1].getParameterDescriptor().getMaxValue().intValue();
            int minLast = params[num - 1].getParameterDescriptor().getMinValue().intValue();
            int lastVal = params[num - 1].getValue().intValue();

            sp = (double) (firstVal - minFirst) / (double) (maxFirst - minFirst) * 100.0;
            ep = (double) (lastVal - minLast) / (double) (maxLast - minLast) * 100.0;
        }
        int valIndex = 0;
        if (inclusive) {
            for (int i = 0; i < num; i++)
                percents[i] = sp + ((ep - sp) / (num - 1)) * i;
        } else {
            for (int i = 0; i < num; i++)
                percents[i] = sp + ((ep - sp) / num) * i;
        }
        for (int i = 0; i < params.length; i++)
            params[i].setValue(ParameterModelUtilities.calcPOR(params[i], percents[valIndex++]));
        return false;
    }
}
