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
public class ParameterScaleCurrentZMTC extends AbstractParameterZMTCommand {
    private float percent;
    private boolean custom;

    public ParameterScaleCurrentZMTC() {
        this(10);
    }

    private ParameterScaleCurrentZMTC(float percent) {
        super(null, null, null, null);
        if (percent == 0) {
            init("Custom", "Scale value to a customized % of current", new String[]{"Percent"}, new String[]{"Percentage to scale current value by"});
            custom = true;
        } else {
            init(String.valueOf((int) percent) + "%", "Scale value to " + String.valueOf((int) percent) + "% of current", null, null);
            this.percent = percent;
            custom = false;
        }
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (custom && index == 0)
            return new FixedLengthTextField("", 6);

        return null;
    }

    public ZMTCommand getNextMode() {
        if (percent == 0)
            return null;
        if (percent >= 200)
            return new ParameterScaleCurrentZMTC(0);

        return new ParameterScaleCurrentZMTC(percent + 10);
    }

    public String getMenuPathString() {
        return ";Scale";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (custom)
            try {
                percent = Math.abs(Float.parseFloat(arguments[0].toString()));
            } catch (NumberFormatException e) {
                throw new CommandFailedException("not a valid percentage");
            }

        EditableParameterModel[] params = getTargets();
        int num = params.length;
        EditableParameterModel p;

        if (num == 0) {
            // try use primary target
            p = getTarget();
            ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(new Object[]{p}), new EditableParameterModel.EditChainValueProvider() {
                public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                    return scaleValue(model);
                }
            });
        } else
            ParameterModelUtilities.dispatchEditChainGroups(ParameterModelUtilities.extractEditableParameterModelChainGroups(params), new EditableParameterModel.EditChainValueProvider() {
                public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                    return scaleValue(model);
                }
            });
    }

    private Integer scaleValue(EditableParameterModel p) throws ParameterUnavailableException {
        int maxv = p.getParameterDescriptor().getMaxValue().intValue();
        int minv = p.getParameterDescriptor().getMinValue().intValue();
        int currv = p.getValue().intValue() - minv;
        int newv = (int) Math.round((minv + (currv * percent) / 100.0));

        if (newv > maxv)
            newv = maxv;

        return IntPool.get(newv);
    }
}
