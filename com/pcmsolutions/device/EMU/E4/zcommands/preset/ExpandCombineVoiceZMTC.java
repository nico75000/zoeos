package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.ZMTCommand;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class ExpandCombineVoiceZMTC extends AbstractEditableVoiceZMTCommand {
    private int mode;

    public ExpandCombineVoiceZMTC() {
        initExpand();
    }

    protected void initExpand() {
        this.mode = 0;
    }

    protected void initCombine() {
        this.mode = 1;
    }

    public ZMTCommand getNextMode() {
        if (mode == 0) {
            ExpandCombineVoiceZMTC o = new ExpandCombineVoiceZMTC();
            o.initCombine();
            return o;
        }
        return null;
    }

    public String getPresentationString() {
        return (mode == 0 ? "Expand" : "Combine");
    }

    public String getDescriptiveString() {
        return (mode == 0 ? "Expand zones to seperate voices" : "Combine voices in group");
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        int num = voices.length;
        Arrays.sort(voices);
        HashSet done = new HashSet();
        for (int n = num - 1; n >= 0; n--) {
            if (!done.contains(voices[n])) {
                if (mode == 0)
                    voices[n].expandVoice();
                else
                    voices[n].combineVoiceGroup();
                done.add(voices[n]);
            }
        }
        return false;
    }

    public int getMaxNumTargets() {
        if (mode == 1)
            return 1;
        return super.getMaxNumTargets();
    }
}

