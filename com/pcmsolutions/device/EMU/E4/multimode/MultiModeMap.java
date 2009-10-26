package com.pcmsolutions.device.EMU.E4.multimode;

import com.pcmsolutions.comms.ByteStreamable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 16:07:14
 * To change this template use Options | File Templates.
 */
public interface MultiModeMap extends ByteStreamable, Serializable {

    public boolean has32();

    public void setMapData(byte[] mapData);

    public Integer getPreset(Integer ch) throws IllegalMidiChannelException;

    public Integer getVolume(Integer ch) throws IllegalMidiChannelException;

    public Integer getPan(Integer ch) throws IllegalMidiChannelException;

    public Integer getSubmix(Integer ch) throws IllegalMidiChannelException;

    public void setPreset(Integer ch, Integer preset) throws IllegalMidiChannelException;

    public void setVolume(Integer ch, Integer volume) throws IllegalMidiChannelException;

    public void setPan(Integer ch, Integer pan) throws IllegalMidiChannelException;

    public void setSubmix(Integer ch, Integer submix) throws IllegalMidiChannelException;

    public MultiModeMap getCopy();

}
