package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.DecrementIcon;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.IncrementIcon;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.TimingUtils;

import javax.swing.*;

public class ParameterUnitStepFromCurrentZMTC extends AbstractParameterZMTCommand {
    private static final int[] steps = new int[]{-1, +1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12};
    private int index;
    private boolean custom = false;

    private static final AbstractZCommandField<FixedLengthTextField, String> inputField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 6), "Step", "Step in units") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Unit step", new ZCommandField[]{inputField});
    }

    public ParameterUnitStepFromCurrentZMTC() {
        init(0);
    }

    private void init(int index) {
        this.index = index;
        custom = false;
    }

    private void initCustom() {
        this.custom = true;
        index = -1;
    }

    public int getMinNumTargets() {
        return 1;
    }

    public ZMTCommand getNextMode() {
        if (custom)
            return null;

        ParameterUnitStepFromCurrentZMTC next = new ParameterUnitStepFromCurrentZMTC();

        if (index < steps.length - 1)
            next.init(index + 1);
        else
            next.initCustom();

        return next;
    }

    public boolean isSuitableAsButton() {
        if (custom)
            return false;

        if (steps[index] == 1 || steps[index] == -1)
            return true;

        return false;
    }

    public boolean isSuitableInToolbar() {
        if (custom)
            return false;

        if (steps[index] == 1 || steps[index] == -1)
            return true;

        return false;
    }

    public String getPresentationString() {
        if (custom)
            return "Custom";
        if (steps[index] == 1) {
            return "Increment";
        } else if (steps[index] == -1) {
            return "Decrement";
        } else {
            return String.valueOf(Math.abs(steps[index])) + " units";
        }
    }

    public String getDescriptiveString() {
        if (custom)
            return "Offset by custom number of units";
        if (steps[index] == 1) {
            return "Increment (+1 units)";
        } else if (steps[index] == -1) {
            return "Decrement (-1 units)";
        } else {
            return "Offset by " + String.valueOf(steps[index]) + " units";
        }
    }

    public Icon getIcon() {
        if (custom)
            return null;
        if (steps[index] == 1)
            return IncrementIcon.INSTANCE;
        if (steps[index] == -1)
            return DecrementIcon.INSTANCE;
        return null;
    }

    public String getMenuPathString() {
        if (custom)
            return ";Offset";

        if (steps[index] == 1 || steps[index] == -1)
            return "";

        if (steps[index] < 0)
            return ";Offset;Down";
        else
            return ";Offset;Up";
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        if (custom) {
            cmdDlg.run(new ZCommandDialog.Executable() {
                public void execute() throws Exception {
                    double unitStep = getUnconstrainedDoubleForField(getTargets().toArray(new ReadableParameterModel[numTargets()]), inputField.getValue());
                    ParameterModelUtilities.offsetModels(getTargets().toArray(new EditableParameterModel[numTargets()]), (int) (Math.round(unitStep)));
                }
            });
        } else {
            double unitStep = steps[index];
            ParameterModelUtilities.offsetModels(getTargets().toArray(new EditableParameterModel[numTargets()]), (int) (Math.round(unitStep)));
        }
        return false;
    }
}
