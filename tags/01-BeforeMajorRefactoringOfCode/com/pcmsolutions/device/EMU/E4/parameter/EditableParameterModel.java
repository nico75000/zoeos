package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableParameterModelZCommandMarker;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 10-Mar-2003
 * Time: 17:07:04
 * To change this template use Options | File Templates.
 */
public interface EditableParameterModel extends ReadableParameterModel, ZCommandProvider {
    public final static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4EditableParameterModelZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.ParameterDefaultZMTC;com.pcmsolutions.device.EMU.E4.zcommands.MaxParameterZMTC;com.pcmsolutions.device.EMU.E4.zcommands.MinParameterZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterRandomizeZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterPercentilesZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterLinearFadeZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterAllToFirstZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterUnitStepFromFirstZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterUnitStepFromCurrentZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterScaleCurrentZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ParameterReflectionZMTC");

    public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException;

    public void setValueString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException;

    public void setValueUnitlessString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException;

    public void defaultValue() throws ParameterUnavailableException, ParameterValueOutOfRangeException;

    public void setValue(EditChainValueProvider ecvp, EditableParameterModel[] chained) throws ParameterUnavailableException, ParameterValueOutOfRangeException;

    public boolean isEditChainableWith(Object o);

    public static interface EditChainValueProvider {
        // might return null to signify no operation
        public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException;
    }
}
