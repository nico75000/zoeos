package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.system.AbstractZMTCommand;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractParameterZMTCommand extends AbstractZMTCommand implements E4EditableParameterModelZCommandMarker {

    protected static final int PARAMETER_NUMERIC_FIELD_LENGTH = 7;

    public AbstractParameterZMTCommand() {
        super(EditableParameterModel.class);
    }

    protected AbstractParameterZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(EditableParameterModel.class, presString, descString, argPresStrings, argDescStrings);
    }

    public EditableParameterModel getTarget() {
        return (EditableParameterModel) target;
    }

    protected static void areHomogenousEditableParameters(Object[] targets) throws ZMTCommandTargetsNotSuitableException {
        for (int i = 0; i < targets.length; i++)
            if (!(targets[i] instanceof EditableParameterModel) || (i > 0 && !(((EditableParameterModel) targets[i - 1]).getParameterDescriptor().getId().equals(((EditableParameterModel) targets[i]).getParameterDescriptor().getId()))))
                throw new ZMTCommandTargetsNotSuitableException();
    }

    protected static void areSpecificEditableParameters(Object[] targets, Integer id) throws ZMTCommandTargetsNotSuitableException {
        for (int i = 0; i < targets.length; i++)
            if (!(targets[i] instanceof EditableParameterModel) || !((EditableParameterModel) targets[i]).getParameterDescriptor().getId().equals(id))
                throw new ZMTCommandTargetsNotSuitableException();
    }

    public EditableParameterModel[] getTargets() {
        if (targets == null)
            return new EditableParameterModel[0];

        int num = targets.length;
        EditableParameterModel[] params = new EditableParameterModel[num];

        for (int n = 0; n < num; n++) {
            params[n] = (EditableParameterModel) targets[n];
        }
        return params;
    }
}
