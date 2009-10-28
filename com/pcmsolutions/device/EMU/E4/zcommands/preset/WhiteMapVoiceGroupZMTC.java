package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.system.IntervalServices;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class WhiteMapVoiceGroupZMTC extends AbstractEditableVoiceZMTCommand {

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "WhiteMapG";
    }

    public String getDescriptiveString() {
        return "Map voices in group on white keys starting from low key of first voice in the group";
    }

    public String getMenuPathString() {
        return ";Key Mapping";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        Integer grp = voices[0].getVoiceParams(new Integer[]{ID.group})[0];
        Integer[] vig = voices[0].getPresetContext().getVoiceIndexesInGroup(voices[0].getPresetNumber(), grp);
        int lowKey = voices[0].getPresetContext().getVoiceParams(voices[0].getPresetNumber(), vig[0], new Integer[]{ID.lowKey})[0].intValue();
        PresetContextMacros.intervalMapGroupKeyWin(voices[0].getPresetContext(), voices[0].getPresetNumber(), grp, new IntervalServices.Mapper.White(lowKey));
        return false;
    }
}

