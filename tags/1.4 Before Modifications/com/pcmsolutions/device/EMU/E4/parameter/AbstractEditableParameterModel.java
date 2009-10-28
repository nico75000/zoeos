package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.device.EMU.DeviceException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 26-Apr-2003
 * Time: 21:44:25
 * To change this template use Options | File Templates.
 */
public abstract class AbstractEditableParameterModel extends AbstractReadableParameterModel implements EditableParameterModel, ZCommandProvider {
    protected boolean expired = false;

    public AbstractEditableParameterModel(GeneralParameterDescriptor pd) {
        super(pd);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ReadableParameterModel)
            return pd.getId().equals(((ReadableParameterModel) obj).getParameterDescriptor().getId());
        else if (obj instanceof Integer)
            return obj.equals(pd.getId());
        return false;
    }

    public abstract void setValue(Integer value) throws ParameterException;

    public abstract void offsetValue(Integer offset) throws ParameterException;

    public abstract void offsetValue(Double offsetAsFOR) throws ParameterException; 

    public void setValueString(String value) throws ParameterException {
            setValue(pd.getValueForString(value));
    }

    public void setValueUnitlessString(String value) throws ParameterException {
            setValue(pd.getValueForUnitlessString(value));
    }

    public void defaultValue() throws ParameterException {
            setValue(pd.getDefaultValue());
    }

    public boolean isExpired() {
        return expired;
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return EditableParameterModel.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    public Class[] getZCommandMarkers() {
        return EditableParameterModel.cmdProviderHelper.getSupportedMarkers();
    }
}
