package com.pcmsolutions.device.EMU.E4.gui.master;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.selections.MasterParameterSelection;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 19-Aug-2003
 * Time: 18:59:05
 * To change this template use Options | File Templates.
 */
public interface MasterParameterSelectionAcceptor {
    public void setSelection(MasterParameterSelection sel);

    public boolean willAcceptCategory(int category);

    public DeviceContext getDevice();
}
