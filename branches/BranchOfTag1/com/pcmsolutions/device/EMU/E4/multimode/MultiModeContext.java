package com.pcmsolutions.device.EMU.E4.multimode;

import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 15:33:53
 * To change this template use Options | File Templates.
 */
public interface MultiModeContext extends Serializable{
    public MultiModeMap getMultimodeMap();

    public MultiModeChannel getMultiModeChannel(Integer channel) throws IllegalMidiChannelException;

    public void setMultimodeMap(MultiModeMap mmMap);

    public void addMultiModeListener(MultiModeListener mml);

    public void removeMultiModeListener(MultiModeListener mml);

    public MultiModeDescriptor getMultiModeDescriptor();

    public boolean has32Channels();

    public Integer getPreset(Integer ch) throws IllegalMidiChannelException;

    public Integer getVolume(Integer ch) throws IllegalMidiChannelException;

    public Integer getPan(Integer ch) throws IllegalMidiChannelException;

    public Integer getSubmix(Integer ch) throws IllegalMidiChannelException;

    public void setPreset(Integer ch, Integer preset) throws IllegalMidiChannelException;

    public void setVolume(Integer ch, Integer volume) throws IllegalMidiChannelException;

    public void setPan(Integer ch, Integer pan) throws IllegalMidiChannelException;

    public void setSubmix(Integer ch, Integer submix) throws IllegalMidiChannelException;

    public void refresh();
}
