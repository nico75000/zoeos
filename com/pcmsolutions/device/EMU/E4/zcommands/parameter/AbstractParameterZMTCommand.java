package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableParameterModelZCommandMarker;
import com.pcmsolutions.system.AbstractZMTCommand;
import com.pcmsolutions.system.TimingUtils;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;
import com.pcmsolutions.system.ZCommandTargetsNotSuitableException;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractParameterZMTCommand extends AbstractZMTCommand<EditableParameterModel> implements E4EditableParameterModelZCommandMarker {

    protected static final int PARAMETER_NUMERIC_FIELD_LENGTH = 7;

    public String getPresentationCategory() {
        return "Parameter";
    }

    public boolean isSortable() {
        return false;
    }

    protected void assertHomogenousEditableParameters() throws ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
        Iterator<EditableParameterModel> i = getTargets().iterator();
        Integer lastId = null;
        while (i.hasNext()) {
            EditableParameterModel p = i.next();
            if (lastId != null && !p.getParameterDescriptor().getId().equals(lastId))
                throw new ZCommandTargetsNotSuitableException();
        }
    }

    protected static void areSpecificEditableParameters(Object[] targets, Integer id) throws ZCommandTargetsNotSuitableException {
        for (int i = 0; i < targets.length; i++)
            if (!(targets[i] instanceof EditableParameterModel) || !((EditableParameterModel) targets[i]).getParameterDescriptor().getId().equals(id))
                throw new ZCommandTargetsNotSuitableException();
    }

    public static double getUnconstrainedDoubleForField(ReadableParameterModel[] models, String field) throws NumberFormatException {
        if (ParameterModelUtilities.areAllOfId(models, ID.delay))
            return TimingUtils.parseMsField_int(field);
        else
            return Double.parseDouble(field);
    }

     public static double getConstrainedDoubleForField(ReadableParameterModel[] models, String field) throws NumberFormatException {
        if (ParameterModelUtilities.areAllOfId(models, ID.delay))
            return TimingUtils.constrainDelay(TimingUtils.parseMsField_int(field));
        else
            return Double.parseDouble(field);
    }
}
