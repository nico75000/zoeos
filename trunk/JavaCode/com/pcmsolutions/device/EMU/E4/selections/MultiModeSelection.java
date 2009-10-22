package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeDescriptor;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 19-May-2003
 * Time: 20:03:20
 * To change this template use Options | File Templates.
 */
public class MultiModeSelection extends AbstractE4Selection {
    private ArrayList channels = new ArrayList();
    private transient MultiModeContext mmc;
    private int[] selCols;

    public MultiModeSelection(DeviceContext sourceDevice, MultiModeContext mmc, int[] selCols, int[] selRows) throws DeviceException {
        super(sourceDevice);
        this.selCols = selCols;
        for (int i = 0, n = selRows.length; i < n; i++)
            channels.add(new Impl_MultiModeChannelSelection(mmc, selCols, selRows[i]));
    }

    public int[] getSelCols() {
        int[] out = new int[selCols.length];
        System.arraycopy(selCols, 0, out, 0, selCols.length);
        return out;
    }

    public MultiModeChannelSelection[] getChannelData() {
        return (MultiModeChannelSelection[]) channels.toArray(new MultiModeChannelSelection[channels.size()]);
    }

    public void render(MultiModeContext mmc, int targetChannel) {
        MultiModeChannelSelection[] chData = getChannelData();
        Integer val;
        Integer ch;
        int numChnls = (mmc.has32Channels() ? 32 : 16);
        for (int i = 0, n = chData.length; i < n; i++) {
            if (targetChannel + i > numChnls)
                break;
            ch = IntPool.get(targetChannel + i);
            val = chData[i].getPreset();
            if (val != null)
                try {
                    mmc.setPreset(ch, val).post();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }

            val = chData[i].getVolume();
            if (val != null)
                try {
                    mmc.setVolume(ch, val).post();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }
            val = chData[i].getPan();
            if (val != null)
                try {
                    mmc.setPan(ch, val).post();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }
            val = chData[i].getSubmix();
            if (val != null)
                try {
                    mmc.setSubmix(ch, val).post();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }
        }
    }

    public interface MultiModeChannelSelection {
        public Integer getChannel();

        public Integer getPreset();

        public Integer getPan();

        public Integer getVolume();

        public Integer getSubmix();

        public String getPresetString();

        public String getPanString();

        public String getVolumeString();

        public String getSubmixString();
    }

    private class Impl_MultiModeChannelSelection implements MultiModeChannelSelection {
        private Integer channel;
        // order of arrays is preset, volume, pan, submix
        private final Integer[] values = new Integer[4];
        private final String[] valueStrings = new String[4];

        public Impl_MultiModeChannelSelection(MultiModeContext mmc, int[] selCols, int row) throws DeviceException {
            channel = IntPool.get(row + 1);
            MultiModeDescriptor mmd = mmc.getMultiModeDescriptor();
            GeneralParameterDescriptor preset_pd = mmd.getPresetParameterDescriptor();
            GeneralParameterDescriptor vol_pd = mmd.getVolumeParameterDescriptor();
            GeneralParameterDescriptor pan_pd = mmd.getPanParameterDescriptor();
            GeneralParameterDescriptor submix_pd = mmd.getSubmixParameterDescriptor();

            MultiModeMap m = mmc.getMultimodeMap();

            for (int i = 0, n = selCols.length; i < n; i++) {
                if (selCols[i] == 0) {
                    try {
                        values[0] = m.getPreset(channel);
                        valueStrings[0] = preset_pd.getStringForValue(values[0]);
                    } catch (IllegalMultimodeChannelException e) {
                        e.printStackTrace();
                    } catch (ParameterValueOutOfRangeException e) {
                        e.printStackTrace();
                    }
                }

                if (selCols[i] == 1) {
                    try {
                        values[1] = m.getVolume(channel);
                        valueStrings[1] = vol_pd.getStringForValue(values[1]);
                    } catch (IllegalMultimodeChannelException e) {
                        e.printStackTrace();
                    } catch (ParameterValueOutOfRangeException e) {
                        e.printStackTrace();
                    }
                }
                if (selCols[i] == 2) {
                    try {
                        values[2] = m.getPan(channel);
                        valueStrings[2] = pan_pd.getStringForValue(values[2]);
                    } catch (IllegalMultimodeChannelException e) {
                        e.printStackTrace();
                    } catch (ParameterValueOutOfRangeException e) {
                        e.printStackTrace();
                    }
                }
                if (selCols[i] == 3) {
                    try {
                        values[3] = m.getSubmix(channel);
                        valueStrings[3] = submix_pd.getStringForValue(values[3]);
                    } catch (IllegalMultimodeChannelException e) {
                        e.printStackTrace();
                    } catch (ParameterValueOutOfRangeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public Integer getChannel() {
            return channel;
        }

        public Integer getPreset() {
            return values[0];
        }

        public Integer getVolume() {
            return values[1];
        }

        public Integer getPan() {
            return values[2];
        }

        public Integer getSubmix() {
            return values[3];
        }

        public String getPresetString() {
            return valueStrings[0];
        }

        public String getVolumeString() {
            return valueStrings[1];
        }

        public String getPanString() {
            return valueStrings[2];
        }

        public String getSubmixString() {
            return valueStrings[3];
        }
    }
}
