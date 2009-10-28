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
public class SpanMapVoiceGroupZMTC extends AbstractEditableVoiceZMTCommand {

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "SpanMapG";
    }

    public String getDescriptiveString() {
        return "Map voices in group on regular key intervals starting on low key of first voice in group and ending on high key of last voice in group";
    }

    public String getMenuPathString() {
        return ";Key Mapping";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        Integer grp = voices[0].getVoiceParams(new Integer[]{ID.group})[0];
        Integer[] vig = voices[0].getPresetContext().getVoiceIndexesInGroup(voices[0].getPresetNumber(), grp);
        int lowKey = voices[0].getPresetContext().getVoiceParams(voices[0].getPresetNumber(), vig[0], new Integer[]{ID.lowKey})[0].intValue();
        int highKey = voices[0].getPresetContext().getVoiceParams(voices[voices.length - 1].getPresetNumber(), vig[vig.length - 1], new Integer[]{ID.highKey})[0].intValue();
        PresetContextMacros.intervalMapGroupKeyWin(voices[0].getPresetContext(), voices[0].getPresetNumber(), grp, new IntervalServices.Mapper.Spanning(lowKey, highKey, vig.length));
        return false;
    }
}

