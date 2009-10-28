package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;

import java.awt.event.KeyEvent;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class NewPresetVoicesZMTC extends AbstractContextEditablePresetZMTCommand {
    private int voiceCount;

    public NewPresetVoicesZMTC() {
        this(1);
    }

    protected NewPresetVoicesZMTC(int voiceCount) {
        this.voiceCount = voiceCount;
    }

    public int getMnemonic() {
        if (voiceCount == 1)
            return KeyEvent.VK_V;

        return super.getMnemonic();
    }

    public boolean isSuitableInToolbar() {
        if (voiceCount == 1)
            return true;
        return false;
    }

    public String getPresentationString() {
        return (voiceCount == 1 ? "AddV" : voiceCount + " voices");
    }

    public String getDescriptiveString() {
        return (voiceCount == 1 ? "Add 1 new voice to preset" : "Add " + voiceCount + " new voices to preset");
    }

    public boolean isSuitableAsButton() {
        if (voiceCount == 1)
            return true;
        return false;
    }

    public ZMTCommand getNextMode() {
        if (voiceCount == 8)
            return null;
        return new NewPresetVoicesZMTC(voiceCount + 1);
    }

    private void newVoice(ContextEditablePreset p) throws PresetException {
        Integer[] samples = new Integer[voiceCount];
        for (int i = 0, n = voiceCount; i < n; i++)
            samples[i] = IntPool.get(0);
        p.newVoices(IntPool.get(voiceCount), samples);
    }

    public String getMenuPathString() {
        if (voiceCount == 1)
            return "";
        return ";Add;Voices";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
        int num = presets.length;
        HashSet done = new HashSet();
        for (int n = 0; n < num; n++) {
            if (!done.contains(presets[n])) {
                newVoice(presets[n]);
                done.add(presets[n]);
            }
        }
        return false;
    }
}

