package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractEditableVoiceZMTCommand extends AbstractZMTCommand implements E4EditableVoiceZCommandMarker {

    protected AbstractEditableVoiceZMTCommand() {
        super(ContextEditablePreset.EditableVoice.class);
    }

    protected AbstractEditableVoiceZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextEditablePreset.EditableVoice.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextEditablePreset.EditableVoice getTarget() {
        return (ContextEditablePreset.EditableVoice) target;
    }

    public ContextEditablePreset.EditableVoice[] getTargets() {
        if (targets == null)
            return new ContextEditablePreset.EditableVoice[0];

        int num = targets.length;
        ContextEditablePreset.EditableVoice[] voices = new ContextEditablePreset.EditableVoice[num];

        for (int n = 0; n < num; n++) {
            voices[n] = (ContextEditablePreset.EditableVoice) targets[n];
        }
        return voices;
    }
}
