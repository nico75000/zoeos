package com.pcmsolutions.device.EMU.E4.multimode;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.system.ZDeviceNotRunningException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 15:33:53
 * To change this template use Options | File Templates.
 */
public interface MultiModeChannel {
    public MultiModeDescriptor getMultiModeDescriptor();

    public Integer getChannel();

    public Integer getPreset();

    public Integer getVolume();

    public Integer getPan();

    public Integer getSubmix();

    public void setPreset(Integer preset);

    public void setVolume(Integer volume);

    public void setPan(Integer pan);

    public void setSubmix(Integer submix);

    public void addMultiModeListener(MultiModeListener mml);

    public void removeMultiModeListener(MultiModeListener mml);

    public EditableParameterModel getPanEditableParameterModel() throws ZDeviceNotRunningException, IllegalParameterIdException;

    public EditableParameterModel getPresetEditableParameterModel() throws IllegalParameterIdException;

    public EditableParameterModel getVolumeEditableParameterModel() throws IllegalParameterIdException;

    public EditableParameterModel getSubmixEditableParameterModel() throws IllegalParameterIdException;

}
