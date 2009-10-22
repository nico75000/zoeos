package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class CopyVoiceZMTC extends AbstractEditableVoiceZMTCommand {
    public String getPresentationString() {
        return "Copy";
    }

    public String getDescriptiveString() {
        return "Copy Voice";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        int num = voices.length;
        for (int n = 0; n < num; n++)
            voices[n].copyVoice();
        return false;
    }
}

