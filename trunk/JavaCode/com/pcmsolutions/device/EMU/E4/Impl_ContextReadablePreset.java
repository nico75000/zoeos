package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextReadablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import java.util.*;


class Impl_ContextReadablePreset extends Impl_ReadablePreset implements ContextReadablePreset, ZCommandProvider {

    static {
        PresetClassManager.addPresetClass(Impl_ContextReadablePreset.class, null, "Readable Preset");
    }

    public Impl_ContextReadablePreset(PresetContext pc, Integer preset) {
        super(pc, preset);
    }

    public ReadablePreset getMostCapableNonContextEditablePreset() {
        return this;
    }

    public boolean equals(Object o) {
        ContextReadablePreset p;
        if (o instanceof ContextReadablePreset) {
            p = (ContextReadablePreset) o;
            if (p.getIndex().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer) {
                if (o.equals(preset))
                    return true;
            }

        return false;
    }

    public void copyLink(Integer srcLink, Integer destPreset) throws PresetException{
        try {
            pc.copyLink(preset, srcLink, destPreset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public PresetContext getPresetContext() {
        return pc;
    }

    public SortedSet<Integer> getPresetIndexesInContext() throws PresetException {
        try {
            return pc.getIndexesInContext();
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public Map<Integer,String> getPresetNamesInContext() throws PresetException {
        try {
            return pc.getContextNamesMap();
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public SortedSet<Integer> findEmptyPresets(Integer reqd, Integer beginIndex, Integer maxIndex) throws PresetException {
        try {
            return pc.findEmpties(reqd, beginIndex, maxIndex);
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void copyPreset(Integer destPreset) throws  PresetException {
        try {
            pc.copy(preset, destPreset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void copyPreset(Integer destPreset, String name) throws PresetException {
        try {
            pc.copy(preset, destPreset, name).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void copyVoice(Integer srcVoice, Integer destPreset) throws PresetException{
        try {
            pc.copyVoice(preset, srcVoice, destPreset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return ContextReadablePreset.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ContextReadablePreset.cmdProviderHelper.getSupportedMarkers();
    }  
}

