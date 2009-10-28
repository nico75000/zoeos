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

    public byte[] getMapData();

    public Integer getPreset(Integer ch) throws IllegalMultimodeChannelException;

    public Integer getVolume(Integer ch) throws IllegalMultimodeChannelException;

    public Integer getPan(Integer ch) throws IllegalMultimodeChannelException;

    public Integer getSubmix(Integer ch) throws IllegalMultimodeChannelException;

    public void setPreset(Integer ch, Integer preset) throws IllegalMultimodeChannelException;

    public void setVolume(Integer ch, Integer volume) throws IllegalMultimodeChannelException;

    public void setPan(Integer ch, Integer pan) throws IllegalMultimodeChannelException;

    public void setSubmix(Integer ch, Integer submix) throws IllegalMultimodeChannelException;

    public MultiModeMap getCopy();

}
