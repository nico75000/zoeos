package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextBasicEditablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

class Impl_ContextBasicEditablePreset extends Impl_ContextReadablePreset implements ContextBasicEditablePreset, ZCommandProvider, IconAndTipCarrier, Comparable {
    static {
        PresetClassManager.addPresetClass(Impl_ContextBasicEditablePreset.class, null, "Basic Editable Preset");
    }

    public Impl_ContextBasicEditablePreset(PresetContext pc, Integer preset) {
       super(pc, preset);
    }

    public boolean equals(Object o) {
        ContextBasicEditablePreset p;
        if (o instanceof ContextBasicEditablePreset) {
            p = (ContextBasicEditablePreset) o;
            if (p.getIndex().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer) {
                if (o.equals(preset))
                    return true;
            }
        return false;
    }

    
    public void erasePreset() throws PresetException {
        try {
            pc.erase(preset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public ReadablePreset getMostCapableNonContextEditablePreset() {
        return this;
    }

    public ContextReadablePreset getContextReadablePresetDowngrade() {
        Impl_ContextReadablePreset np = new Impl_ContextReadablePreset(pc, preset);
        np.stringFormatExtended = stringFormatExtended;
        return np;

    }

    public void setPresetName(String name) throws  PresetException {
        try {
            pc.setName(preset, name).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return ContextBasicEditablePreset.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ContextBasicEditablePreset.cmdProviderHelper.getSupportedMarkers();
    }

}

