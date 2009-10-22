package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.*;

import java.util.Iterator;

public class ParameterReflectionZMTC extends AbstractParameterZMTCommand {
    private final int mode;
    private float abs_pivot;
    private String pivotStr;
    private float localCenter;

    private static final ZCommandField<FixedLengthTextField, String> numericField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField(PARAMETER_NUMERIC_FIELD_LENGTH), "Pivot value", "The pivot value for the reflection operation") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    private static final String[] presStrings = new String[]{"Reflect about absolute center", "Reflect about local center", "Reflect about custom"};
    private static final String[] descStrings = new String[]{"Reflect about the centre of absolute range", "Reflect about the centre of the selected range", "Reflect about custom"};

    static {
        cmdDlg.init(presStrings[2], new ZCommandField[]{numericField});
    }

    public ParameterReflectionZMTC() {
        this(0);
    }

    private ParameterReflectionZMTC(int mode) {
        this.mode = mode;
    }

    public boolean isSuitableInToolbar() {
        return false;
    }

    public String getPresentationString() {
        return presStrings[mode];
    }

    public String getDescriptiveString() {
        return descStrings[mode];
    }

    public ZMTCommand getNextMode() {
        if (mode == 2)
            return null;
        return new ParameterReflectionZMTC(mode + 1);
    }

    public int getMinNumTargets() {
        if (mode == 1)
            return 2;
        return super.getMinNumTargets();
    }

    public void acceptTargets() throws ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
        super.acceptTargets();
        assertHomogenousEditableParameters();
        if (mode == 1) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            Iterator<EditableParameterModel> i = getTargets().iterator();
            while (i.hasNext()) {
                EditableParameterModel model = i.next();
                int val = 0;
                try {
                    val = model.getValue().intValue();
                } catch (ParameterException e) {
                    throw new ZCommandTargetsNotSuitableException();
                }
                if (val > max)
                    max = val;
                if (val < min)
                    min = val;
            }
            localCenter = min + ((max - min) / (float) 2.0);
        }
    }

    public String getMenuPathString() {
        return ";Reflect";
    }

    public boolean handleTarget(EditableParameterModel p, int total, int curr) throws Exception {

        float abs_maxv = (float) p.getParameterDescriptor().getMaxValue().intValue();
        float abs_minv = (float) p.getParameterDescriptor().getMinValue().intValue();
        float abs_val = p.getValue().intValue();

        if (mode == 0)       // absolute center
            abs_pivot = abs_minv + (abs_maxv - abs_minv) / 2;
        else if (mode == 1) {           // local center
            abs_pivot = localCenter;
        } else { // from argument[0]                   // custom pivot
            numericField.getComponent().setText("");
            if (curr == 0) {
                if (cmdDlg.run(null, 1, 1) != ZCommandDialog.COMPLETED)
                    return false;
                else
                    pivotStr = numericField.getValue();
            }

            int temp_pivot;
            try {
                temp_pivot = p.getParameterDescriptor().getValueForUnitlessString(pivotStr).intValue();
            } catch (Exception e) {
                throw new CommandFailedException("pivot is invalid");
            }
            if (!p.getParameterDescriptor().isValidValue(IntPool.get(temp_pivot)))
                throw new CommandFailedException("pivot value is not valid");
            abs_pivot = temp_pivot;
        }

        float nv = abs_pivot + (abs_pivot - abs_val);

        if (nv > abs_maxv)
            nv = abs_minv + (nv - abs_maxv);
        else if (nv < abs_minv)
            nv = nv + abs_maxv - (abs_minv - nv);

        p.setValue(IntPool.get(Math.round(nv)));
        return true;
    }
}
