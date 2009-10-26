package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextBasicEditablePresetZCommandMarker;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZUtilities;

class Impl_ContextBasicEditablePreset extends Impl_ContextReadablePreset implements ContextBasicEditablePreset, ZCommandProvider, IconAndTipCarrier, Comparable {
    private static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextBasicEditablePresetZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.ErasePresetZMTC;com.pcmsolutions.device.EMU.E4.zcommands.RenamePresetZC;com.pcmsolutions.device.EMU.E4.zcommands.SpecialPresetNamingZMTC;com.pcmsolutions.device.EMU.E4.zcommands.RenamePresetAllZMTC;");

    static {
        PresetClassManager.addPresetClass(Impl_ContextBasicEditablePreset.class, null, "Basic Editable Preset");
    }

    public Impl_ContextBasicEditablePreset(PresetContext pc, Integer preset) {
        this(pc, preset, null);
    }

    public Impl_ContextBasicEditablePreset(PresetContext pc, Integer preset, DesktopEditingMediator dem) {
        super(pc, preset, dem);
    }

    public boolean equals(Object o) {
        ContextBasicEditablePreset p;
        if (o instanceof ContextBasicEditablePreset) {
            p = (ContextBasicEditablePreset) o;
            if (p.getPresetNumber().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer) {
                if (o.equals(preset))
                    return true;
            }
        return false;
    }

    public void erasePreset() throws NoSuchPresetException, PresetEmptyException {
        try {
            pc.erasePreset(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }
    public ReadablePreset getMostCapableNonContextEditablePresetDowngrade() {
          return this;
      }

    public ContextReadablePreset getContextReadablePresetDowngrade() {
        Impl_ContextReadablePreset np = new Impl_ContextReadablePreset(pc, preset);
        np.dem = dem;
        np.stringFormatExtended = stringFormatExtended;
        return np;

    }

    public void setPresetName(String name) throws NoSuchPresetException, PresetEmptyException {
        try {
            pc.setPresetName(preset, name);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void lockPresetWrite() throws NoSuchPresetException, NoSuchContextException {
        pc.lockPresetWrite(preset);
    }

    public ZCommand[] getZCommands() {
        ZCommand[] superCmdObjects = super.getZCommands();

        ZCommand[] cmdObjects = cmdProviderHelper.getCommandObjects(this);

        return ZUtilities.concatZCommands(superCmdObjects, cmdObjects);
    }
}

