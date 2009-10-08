package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.   e4mu
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ParameterReflectionZMTC extends AbstractParameterZMTCommand {
    private int mode = 0;
    private float pivot;
    private String pivotStr;
    private float localCenter;

    private static final String[] presStrings = new String[]{"Reflect about absolute center", "Reflect about local center", "Reflect about custom"};
    private static final String[] descStrings = new String[]{"Reflect current value about the centre value of the parameter's range", "Reflect current value about the centre of the selected values range", "Reflect current value about custom value"};
    private static final String[][] argPresStrings = new String[][]{null, null, new String[]{"Pivot value"}};
    private static final String[][] argDescStrings = new String[][]{null, null, new String[]{"The pivot value for the reflection operation"}};

    public ParameterReflectionZMTC() {
        this(0);
    }

    private ParameterReflectionZMTC(int mode) {
        super(presStrings[mode], descStrings[mode], argPresStrings[mode], argDescStrings[mode]);
        this.mode = mode;
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

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (mode == 2)
            return new FixedLengthTextField(PARAMETER_NUMERIC_FIELD_LENGTH);

        return null;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        areHomogenousEditableParameters(targets);
        super.setTargets(targets);
    }

    public String getMenuPathString() {
        return ";Reflect";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (mode == 2)
            pivotStr = arguments[0].toString();

        EditableParameterModel[] params = getTargets();
        int num = params.length;
        EditableParameterModel p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                if (p == null)
                    throw new CommandFailedException("Null Target");
                ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(new Object[]{p}), new EditableParameterModel.EditChainValueProvider() {
                    public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                        try {
                            return reflectValue(model);
                        } catch (ParameterUnavailableException e) {
                            e.printStackTrace();
                        } catch (CommandFailedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            } else {
                if (mode == 1) {
                    int min = Integer.MAX_VALUE;
                    int max = Integer.MIN_VALUE;
                    for (int i = 0; i < params.length; i++) {
                        int val = params[i].getValue().intValue();
                        if (val > max)
                            max = val;
                        if (val < min)
                            min = val;
                    }
                    localCenter = min + (max - min) / (float) 2.0;
                }


                ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(params), new EditableParameterModel.EditChainValueProvider() {
                    public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                        try {
                            return reflectValue(model);
                        } catch (ParameterUnavailableException e) {
                            e.printStackTrace();
                        } catch (CommandFailedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            }

        } catch (ParameterUnavailableException e) {
            e.printStackTrace();
        }
    }

    private Integer reflectValue(EditableParameterModel p) throws ParameterUnavailableException, CommandFailedException {
        float maxv = (float) p.getParameterDescriptor().getMaxValue().intValue();
        float minv = (float) p.getParameterDescriptor().getMinValue().intValue();

        float val = (float) p.getValue().intValue() - minv;

        float norm_max = maxv - minv;

        if (mode == 0)
            pivot = minv + norm_max / 2;
        else if (mode == 1) {
            pivot = localCenter;
        } else { // from argument[0]
            int temp_pivot;
            try {
                temp_pivot = p.getParameterDescriptor().getValueForUnitlessString(pivotStr).intValue();
            } catch (Exception e) {
                throw new CommandFailedException("pivot is invalid");
            }

            if (!p.getParameterDescriptor().isValidValue(IntPool.get(temp_pivot)))
                throw new CommandFailedException("pivot value is not valid");
            pivot = temp_pivot - minv;
        }

        float nv = 2 * pivot - val;

        if (nv > norm_max)
            nv = nv - norm_max;

        if (nv < 0)
            nv = nv + norm_max;

        return IntPool.get(Math.round(minv + nv));
    }
}
