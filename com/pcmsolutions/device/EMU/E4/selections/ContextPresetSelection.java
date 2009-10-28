package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
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
public class ContextPresetSelection extends AbstractE4Selection {
    protected IsolatedPreset[] presets;
    protected ReadablePreset[] readablePresets;
    protected PresetContext presetContext;

    public ContextPresetSelection(DeviceContext d, PresetContext pc, ReadablePreset[] presets) {
        super(d);
        this.readablePresets = presets;
        this.presetContext = pc;
    }

    public PresetContext getPresetContext() {
        return presetContext;
    }

    public int presetCount() {
        return readablePresets.length;
    }

    public IsolatedPreset[] getIsolatedPresets() {
        if (presets == null)
            createIsolatedPresets();
        return presets;
    }

    public IsolatedPreset getIsolatedPreset(int i) {
        if (presets == null)
            presets = new IsolatedPreset[readablePresets.length];

        if (i >= 0 && i < readablePresets.length) {
            if (presets[i] == null)
                try {
                    presets[i] = readablePresets[i].getIsolated();
                    return presets[i];
                } catch (EmptyException e) {
                    e.printStackTrace();
                } catch (PresetException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    public ReadablePreset[] getReadablePresets() {
        return (ReadablePreset[]) Arrays.asList(readablePresets).toArray(new ReadablePreset[readablePresets.length]);
    }
   public Integer[] getPresetIndexes(){
       return PresetContextMacros.extractPresetIndexes(readablePresets);
   }
    protected void createIsolatedPresets() {
        if (presets == null) {
            presets = new IsolatedPreset[readablePresets.length];
        }

        for (int i = 0,j = readablePresets.length; i < j; i++) {
            try {
                if (presets[i] == null)
                    presets[i] = readablePresets[i].getIsolated();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
