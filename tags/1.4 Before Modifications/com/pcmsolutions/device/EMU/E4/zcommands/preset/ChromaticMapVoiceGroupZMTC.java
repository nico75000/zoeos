package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntervalServices;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class ChromaticMapVoiceGroupZMTC extends AbstractEditableVoiceZMTCommand {

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "ChromaticMapG";
    }

    public String getDescriptiveString() {
        return "Map voices in group on chromatic keys starting at low key of first voice in the group";
    }

    public String getMenuPathString() {
        return ";Key Mapping";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice voice, int total, int curr) throws Exception {
        try {
            Integer grp = voice.getVoiceParams(new Integer[]{ID.group})[0];
            Integer[] vig = voice.getPresetContext().getVoiceIndexesInGroup(voice.getPresetNumber(), grp);
            int lowKey = voice.getPresetContext().getVoiceParams(voice.getPresetNumber(), vig[0], new Integer[]{ID.lowKey})[0].intValue();
            PresetContextMacros.intervalMapGroupKeyWin(voice.getPresetContext(), voice.getPresetNumber(), grp, new IntervalServices.Mapper.Chromatic(lowKey));
        } catch (Exception e) {
            throw new CommandFailedException(e.getMessage());
        }
        return false;
    }
}

