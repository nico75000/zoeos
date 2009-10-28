package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;
import com.pcmsolutions.system.ZMTCommand;

import java.util.Iterator;

public class ParameterUnitStepFromFirstZMTC extends AbstractParameterZMTCommand {
    private static final int[] steps = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12};
    private int index;
    private boolean custom;

    private static final ZCommandField<FixedLengthTextField, String> numericField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField(6), "Step in units", "Step in units") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Custom unit step", new ZCommandField[]{numericField});
    }

    public ParameterUnitStepFromFirstZMTC() {
        initStep(0);
    }

    private void initStep(int index) {
        this.custom = false;
        this.index = index;
    }

    private void initCustom() {
        this.custom = true;
        this.index = -1;
    }


    public int getMinNumTargets() {
        return 2;
    }

    public ZMTCommand getNextMode() {
        if (custom)
            return null;

        ParameterUnitStepFromFirstZMTC next = new ParameterUnitStepFromFirstZMTC();

        if (index < steps.length - 1)
            next.initStep(index + 1);
        else
            next.initCustom();

        return next;
    }

    public String getPresentationString() {
        if (custom)
            return "Custom";
        else
            return String.valueOf((int) Math.abs(steps[index])) + " units";
    }

    public String getDescriptiveString() {
        if (custom)
            return "Step from first using a custom provided step";
        else
            return "Step from first by " + String.valueOf(steps[index]) + " units";
    }

    public String getMenuPathString() {
        if (custom == true)
            return ";Step";
        if (steps[index] < 0)
            return ";Step;Down from first";
        else
            return ";Step;Up from first";
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        if (custom) {
            cmdDlg.run(new ZCommandDialog.Executable() {
                public void execute() throws Exception {
                    double unitStep = getUnconstrainedDoubleForField(getTargets().toArray(new ReadableParameterModel[numTargets()]), numericField.getValue());
                    stepModels(unitStep);
                }
            });
        } else
            stepModels(steps[index]);
        return false;
    }

    private void stepModels(double offsetInUnits) throws ZCommandTargetsNotSpecifiedException, ParameterException {
        int index = 0;
        double FOR = 0;
        for (Iterator<EditableParameterModel> i = getTargets().iterator(); i.hasNext(); index++) {
            EditableParameterModel p = i.next();
            if (index == 0)
                FOR = ParameterModelUtilities.getFOR(p);
            else {
                int maxv = p.getParameterDescriptor().getMaxValue().intValue();
                int minv = p.getParameterDescriptor().getMinValue().intValue();
                double offsetAsFOR = offsetInUnits / (maxv - minv);
                p.setValue(ParameterModelUtilities.calcFOR(p, FOR));
                p.offsetValue(new Double(offsetAsFOR * index));
            }
        }
    }
}
