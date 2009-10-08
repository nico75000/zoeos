package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZUtilities;

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

    public abstract void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException;

    public void setValueString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        setValue(pd.getValueForString(value));
    }

    public void setValueUnitlessString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        setValue(pd.getValueForUnitlessString(value));
    }

    public void defaultValue() throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        setValue(pd.getDefaultValue());
    }

    public void setValue(EditChainValueProvider ecvp, EditableParameterModel[] modelChain) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        for (int i = 0; i < modelChain.length; i++)
            modelChain[i].setValue(ecvp.getValue(modelChain[i], null));
    }

    public boolean isEditChainableWith(Object o) {
        if (o != null && o.getClass().equals(this.getClass()))
            return true;
        else if (o instanceof EditableParameterModelGroup && ((EditableParameterModelGroup) o).getWrappedObjects()[0].getClass().equals(this.getClass()))
            return true;

        return false;
    }

    public boolean isExpired() {
        return expired;
    }

    public ZCommand[] getZCommands() {
        return ZUtilities.concatZCommands(super.getZCommands(), EditableParameterModel.cmdProviderHelper.getCommandObjects(this));
    }
}
