package com.pcmsolutions.device.EMU.E4.multimode;

import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 17:24:00
 * To change this template use Options | File Templates.
 */
public interface MultiModeDescriptor extends Serializable {
    public Integer getMaxChannel();

    public Integer getMaxPreset();

    public GeneralParameterDescriptor getSubmixParameterDescriptor();

    public GeneralParameterDescriptor getPanParameterDescriptor();

    public GeneralParameterDescriptor getVolumeParameterDescriptor();

    public GeneralParameterDescriptor getPresetParameterDescriptor();
}
