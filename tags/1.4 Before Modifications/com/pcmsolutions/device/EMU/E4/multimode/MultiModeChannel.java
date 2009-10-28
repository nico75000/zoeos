package com.pcmsolutions.device.EMU.E4.multimode;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.AuditionManager;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.AuditioningDisabledException;
import com.pcmsolutions.system.tasking.Ticket;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 15:33:53
 * To change this template use Options | File Templates.
 */
public interface MultiModeChannel {
    public MultiModeDescriptor getMultiModeDescriptor();

    public Ticket audition();

    public Integer getChannel();

    public Integer getPreset() throws ParameterException;

    public Integer getVolume() throws ParameterException;

    public Integer getPan() throws ParameterException;

    public Integer getSubmix() throws ParameterException;

    public void setPreset(Integer preset) throws ParameterException;

    public void setVolume(Integer volume) throws ParameterException;

    public void setPan(Integer pan) throws ParameterException;

    public void setSubmix(Integer submix) throws ParameterException;

    public void offsetPreset(Integer offset) throws ParameterException;

    public void offsetVolume(Integer offset) throws ParameterException;

    public void offsetPan(Integer offset) throws ParameterException;

    public void offsetSubmix(Integer offset) throws ParameterException;

    public void offsetPreset(Double offsetAsFOR) throws ParameterException;

    public void offsetVolume(Double offsetAsFOR) throws ParameterException;

    public void offsetPan(Double offsetAsFOR) throws ParameterException;

    public void offsetSubmix(Double offsetAsFOR) throws ParameterException;

    public void addMultiModeListener(MultiModeListener mml);

    public void removeMultiModeListener(MultiModeListener mml);

    public EditableParameterModel getPanEditableParameterModel() throws ParameterException;

    public EditableParameterModel getPresetEditableParameterModel() throws ParameterException;

    public EditableParameterModel getVolumeEditableParameterModel() throws ParameterException;

    public EditableParameterModel getSubmixEditableParameterModel() throws ParameterException;

}
