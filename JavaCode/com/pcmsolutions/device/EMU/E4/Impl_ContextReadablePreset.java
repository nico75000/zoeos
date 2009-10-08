package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextReadablePresetZCommandMarker;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


class Impl_ContextReadablePreset extends Impl_ReadablePreset implements ContextReadablePreset, ZCommandProvider {
    private static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextReadablePresetZCommandMarker.class, /*"com.pcmsolutions.device.EMU.E4.zcommands.CopyBlockContextPresetsZMTC;*/"com.pcmsolutions.device.EMU.E4.zcommands.CopyContextPresetZC;com.pcmsolutions.device.EMU.E4.zcommands.CopyRangeContextPresetZC;com.pcmsolutions.device.EMU.E4.zcommands.CopyBlockContextPresetsZMTC;com.pcmsolutions.device.EMU.E4.zcommands.CopyDeepContextPresetZMTC;");

    static {
        PresetClassManager.addPresetClass(Impl_ContextReadablePreset.class, null, "Readable Preset");
    }

    public Impl_ContextReadablePreset(PresetContext pc, Integer preset) {
        this(pc, preset, null);
    }

    public Impl_ContextReadablePreset(PresetContext pc, Integer preset, DesktopEditingMediator dem) {
        super(pc, preset, dem);
    }
    public ReadablePreset getMostCapableNonContextEditablePresetDowngrade() {
          return this;
      }

    public boolean equals(Object o) {
        ContextReadablePreset p;
        if (o instanceof ContextReadablePreset) {
            p = (ContextReadablePreset) o;
            if (p.getPresetNumber().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer) {
                if (o.equals(preset))
                    return true;
            }

        return false;
    }

    public void copyLink(Integer srcLink, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException {
        try {
            pc.copyLink(preset, srcLink, destPreset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public PresetContext getPresetContext() {
        return pc;
    }

    public Set getPresetIndexesInContext() {
        try {
            return pc.getPresetIndexesInContext();
        } catch (NoSuchContextException e) {
            return new HashMap().keySet();
        }
    }

    public Map getPresetNamesInContext() {
        try {
            return pc.getPresetNamesInContext();
        } catch (NoSuchContextException e) {
            return new HashMap();
        }
    }

    public List findEmptyPresets(Integer reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException {
        return pc.findEmptyPresetsInContext(reqd, beginIndex, maxIndex);
    }

    public boolean presetEmpty(Integer preset) throws NoSuchPresetException {
        try {
            return pc.isPresetEmpty(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void copyPreset(Integer destPreset) throws NoSuchPresetException, PresetEmptyException {
        try {
            pc.copyPreset(preset, destPreset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void copyPreset(Integer destPreset, String name) throws NoSuchPresetException, PresetEmptyException {
        try {
            pc.copyPreset(preset, destPreset, name);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void copyVoice(Integer srcVoice, Integer destPreset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException {
        try {
            pc.copyVoice(preset, srcVoice, destPreset, group);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void getVoiceMultiSample(Integer srcVoice, Integer destPreset, Integer destVoice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
        pc.getVoiceMultiSample(preset, srcVoice, destPreset, destVoice);
    }

    public ZCommand[] getZCommands() {
        return ZUtilities.concatZCommands(super.getZCommands(), this.cmdProviderHelper.getCommandObjects(this));
    }
}

