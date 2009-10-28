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
public class ChromaticMapVoicesZMTC extends AbstractEditableVoiceZMTCommand {
    public int getMinNumTargets() {
        return 2;
    }

    public String getPresentationString() {
        return "ChromaticMapV";
    }

    public String getDescriptiveString() {
        return "Map voices on chromatic keys starting at low key of first voice";
    }

    public String getMenuPathString() {
        return ";Key Mapping";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        PresetContextMacros.intervalMapVoiceKeyWin(voices, new IntervalServices.Mapper.Chromatic(voices[0].getVoiceParams(new Integer[]{ID.lowKey})[0].intValue()));
        return false;
    }
}

