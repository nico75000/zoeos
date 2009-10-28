package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.ZMTCommand;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterScaleCurrentZMTC extends AbstractParameterZMTCommand {
    private final static float[] percentages = new float[]{110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 90, 80, 70, 60, 50, 40, 30, 20, 10};
    private final int index;
    private final boolean custom;

    private static final AbstractZCommandField<FixedLengthTextField, String> inputField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 6), "Percent", "Percentage to scale by (100% = identity)") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Scale", new ZCommandField[]{inputField});
    }


    public ParameterScaleCurrentZMTC() {
        this(0);
    }

    private ParameterScaleCurrentZMTC(int index) {
        this.index = index;
        custom = (index >= percentages.length);
    }

    public boolean isSuitableInToolbar() {
        return false;
    }

    public String getPresentationString() {
        if (custom)
            return "Custom";
        else
            return String.valueOf((int) (percentages[index] > 100 ? percentages[index] - 100 : percentages[index])) + "%";
    }

    public String getDescriptiveString() {
        if (custom)
            return "Custom";
        return "Scale to " + String.valueOf((int) percentages[index]) + "% of current";
    }

    public ZMTCommand getNextMode() {
        if (custom)
            return null;
        return new ParameterScaleCurrentZMTC(index + 1);
    }

    public String getMenuPathString() {
        if (custom)
            return ";Scale";
        if (percentages[index] < 100)
            return ";Scale;Down to";
        return ";Scale;Up by";
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        if (custom) {
            cmdDlg.run(new ZCommandDialog.Executable() {
                public void execute() throws Exception {
                    float percent = Math.abs(Float.parseFloat(inputField.getValue()));
                    ParameterModelUtilities.scaleValues(getTargets(), percent);
                }
            });
        } else {
            ParameterModelUtilities.scaleValues(getTargets(), percentages[index]);
        }
        return false;
    }
}
