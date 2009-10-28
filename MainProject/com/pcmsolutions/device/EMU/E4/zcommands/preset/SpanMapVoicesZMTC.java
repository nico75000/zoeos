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
public class SpanMapVoicesZMTC extends AbstractEditableVoiceZMTCommand {
    public int getMinNumTargets() {
        return 2;
    }

    public String getPresentationString() {
        return "SpanMapV";
    }

    public String getDescriptiveString() {
        return "Map voices on regular key intervals starting on low key of first voice and ending on high key of last voice";
    }

    public String getMenuPathString() {
        return ";Key Mapping";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        int low = voices[0].getVoiceParams(new Integer[]{ID.lowKey})[0].intValue();
        int high = voices[voices.length - 1].getVoiceParams(new Integer[]{ID.highKey})[0].intValue();
        PresetContextMacros.intervalMapVoiceKeyWin(voices, new IntervalServices.Mapper.Spanning(low, high, voices.length));
        return false;
    }
}

