package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class MatchVoiceKeyToSampleNameZMTC extends AbstractEditableVoiceZMTCommand {
    public String getPresentationString() {
        return "Match";
    }

    public String getDescriptiveString() {
        return "Try match original key from sample name";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        for (int i = 0; i < voices.length; i++)
            voices[i].trySetOriginalKeyFromSampleName();
        return false;
    }

    public int getMinNumTargets() {
        return 1;
    }
}

