package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.system.IntPool;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 12-Sep-2004
 * Time: 20:29:18
 */
class Impl_MultiModeMap implements MultiModeMap, Serializable {
    static final long serialVersionUID = 4602025267202652070L;
    private byte[] mapData;
    private boolean has32Channels = false;

    public Impl_MultiModeMap(byte[] mapData) {
        setMapData(mapData);
    }

    public Impl_MultiModeMap(Impl_MultiModeMap mmMap) {
        byte[] in = mmMap.mapData;
        byte[] mapData = new byte[in.length];
        System.arraycopy(in, 0, mapData, 0, in.length);
        setMapData(mapData);
    }

    public void setMapData(byte[] mapData) {
        if (mapData.length < 128)
            throw new IllegalArgumentException();
        this.mapData = mapData;
        if (mapData.length == 256)
            has32Channels = true;
    }

    public byte[] getMapData() {
        return (byte[])mapData.clone();
    }

    public String toString() {
        return "MultiModeMap";
    }

    public Integer getPan(Integer ch) throws IllegalMultimodeChannelException {
        assertCh(ch);
        int val = mapData[(ch.intValue() - 1) * 8 + 4];
        // another E4 sysex inconsistency!!
        if (val > 63)
            val -= 128;
        return IntPool.get(val);
    }

    public Integer getPreset(Integer ch) throws IllegalMultimodeChannelException {
        assertCh(ch);

        return SysexHelper.DataIn(mapData[(ch.intValue() - 1) * 8], mapData[(ch.intValue() - 1) * 8 + 1]);
    }

    public Integer getSubmix(Integer ch) throws IllegalMultimodeChannelException {
        assertCh(ch);
        return SysexHelper.DataIn(mapData[(ch.intValue() - 1) * 8 + 6], mapData[(ch.intValue() - 1) * 8 + 7]);
    }

    public Integer getVolume(Integer ch) throws IllegalMultimodeChannelException {
        assertCh(ch);
        return SysexHelper.DataIn(mapData[(ch.intValue() - 1) * 8 + 2], mapData[(ch.intValue() - 1) * 8 + 3]);
    }

    public void setPan(Integer ch, Integer pan) throws IllegalMultimodeChannelException {
        assertCh(ch);
        SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8 + 4, pan);
    }

    public void setPreset(Integer ch, Integer preset) throws IllegalMultimodeChannelException {
        assertCh(ch);
        SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8, preset);
    }

    public void setSubmix(Integer ch, Integer submix) throws IllegalMultimodeChannelException {
        assertCh(ch);
        SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8 + 6, submix);
    }

    public MultiModeMap getCopy() {
        return new Impl_MultiModeMap(this);
    }

    public void setVolume(Integer ch, Integer volume) throws IllegalMultimodeChannelException {
        assertCh(ch);
        SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8 + 2, volume);
    }

    public boolean has32() {
        return has32Channels;
    }

    private void assertCh(Integer ch) throws IllegalMultimodeChannelException {
        if (ch.intValue() < 1 || (has32Channels && ch.intValue() > 32) || (!has32Channels && ch.intValue() > 16))
            throw new IllegalMultimodeChannelException(ch);
    }

    public ByteArrayInputStream getByteStream() {
        return new ByteArrayInputStream(mapData);
    }
}
