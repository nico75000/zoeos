package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.comms.RemoteMessagingException;

public interface ParameterEditLoader {
    public ParameterEditLoader add(Integer id, Integer value);

    public ParameterEditLoader add(Integer[] ids, Integer[] values);

    public ParameterEditLoader reset();

    public ParameterEditLoader selPreset(Integer preset);

    public ParameterEditLoader selLink(Integer preset, Integer link);

    public ParameterEditLoader selVoice(Integer preset, Integer voice);

    public ParameterEditLoader selGroup(Integer preset, Integer group);

    public ParameterEditLoader selZone(Integer preset, Integer voice, Integer zone);

    public byte[] getBytes();

    public void dispatch() throws com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException;
}

