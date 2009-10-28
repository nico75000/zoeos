package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class DeleteVoiceZMTC extends AbstractEditableVoiceZMTCommand {

    public String getPresentationString() {
        return "Delete";
    }

    public String getDescriptiveString() {
        return "Delete voice";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        int num = voices.length;
        Arrays.sort(voices);
        for (int n = num - 1; n >= 0; n--)
            voices[n].removeVoice();
        return false;
    }
}

