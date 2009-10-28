package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 19:25:16
 * To change this template use Options | File Templates.
 */
public class ZoneSelection extends AbstractE4Selection {
    protected IsolatedPreset.IsolatedVoice.IsolatedZone[] zones;
    protected ReadablePreset.ReadableVoice.ReadableZone[] readableZones;

    protected ReadablePreset preset;

    public ZoneSelection(DeviceContext dc, ReadablePreset.ReadableVoice.ReadableZone[] readableZones) {
        super(dc);
        this.readableZones = readableZones;
    }

    public int zoneCount() {
        return readableZones.length;
    }

    public IsolatedPreset.IsolatedVoice.IsolatedZone[] getIsolatedZones() {
        if (zones == null) {
            zones = new IsolatedPreset.IsolatedVoice.IsolatedZone[readableZones.length];
        }
        for (int i = 0,j = readableZones.length; i < j; i++)
            if (zones[i] == null)
                zones[i] = getIsloatedZone(i);

        return zones;
    }

    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsloatedZone(int i) {
        if (zones == null)
            zones = new IsolatedPreset.IsolatedVoice.IsolatedZone[readableZones.length];

        if (i >= 0 && i < readableZones.length) {
            if (zones[i] == null)
                try {
                    zones[i] = readableZones[i].getIsolated();
                    return zones[i];
                } catch (EmptyException e) {
                    e.printStackTrace();
                } catch (PresetException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    public ReadablePreset.ReadableVoice.ReadableZone[] getReadableZones() {
        return (ReadablePreset.ReadableVoice.ReadableZone[]) Arrays.asList(readableZones).toArray(new ReadablePreset.ReadableVoice.ReadableZone[readableZones.length]);
    }
}
