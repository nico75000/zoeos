package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractReadableVoiceZMTCommand extends AbstractZMTCommand implements E4ReadableVoiceZCommandMarker {
    protected AbstractReadableVoiceZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ReadablePreset.ReadableVoice.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ReadablePreset.ReadableVoice getTarget() {
        return (ReadablePreset.ReadableVoice) target;
    }

    public ReadablePreset.ReadableVoice[] getTargets() {
        if (targets == null)
            return new ReadablePreset.ReadableVoice[0];

        int num = targets.length;
        ReadablePreset.ReadableVoice[] voices = new ReadablePreset.ReadableVoice[num];

        for (int n = 0; n < num; n++) {
            voices[n] = (ReadablePreset.ReadableVoice) targets[n];
        }
        return voices;
    }
}
