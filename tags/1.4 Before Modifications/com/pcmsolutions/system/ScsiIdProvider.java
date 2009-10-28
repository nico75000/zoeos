package com.pcmsolutions.system;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Sep-2003
 * Time: 00:45:36
 * To change this template use Options | File Templates.
 */
public interface ScsiIdProvider {
    public int getScsiId() throws  DeviceException, ParameterException;
}
