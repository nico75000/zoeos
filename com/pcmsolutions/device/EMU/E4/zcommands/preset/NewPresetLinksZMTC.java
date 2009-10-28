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
public class NewPresetLinksZMTC extends AbstractContextEditablePresetZMTCommand {
    private int linkCount;

    public NewPresetLinksZMTC() {
        this(1);
    }

    public NewPresetLinksZMTC(int linkCount) {
        this.linkCount = linkCount;
    }

    public boolean isSuitableAsButton() {
        if (linkCount == 1)
            return true;
        return false;
    }

    public boolean isSuitableInToolbar() {
        if (linkCount == 1)
            return true;
        return false;
    }

    public String getPresentationString() {
        return (linkCount == 1 ? "AddL" : linkCount + " links");
    }

    public String getDescriptiveString() {
        return (linkCount == 1 ? "Add 1 new link to preset" : "Add " + linkCount + " new links to preset");
    }

    public int getMnemonic() {
        if (linkCount == 1)
            return KeyEvent.VK_L;

        return super.getMnemonic();
    }

    public ZMTCommand getNextMode() {
        if (linkCount == 8)
            return null;
        return new NewPresetLinksZMTC(linkCount + 1);
    }

    private void newLink(ContextEditablePreset p) throws PresetException {
        Integer[] presets = new Integer[linkCount];
        for (int i = 0, n = linkCount; i < n; i++)
            presets[i] = IntPool.get(0);

        p.newLinks(presets);
    }

    public String getMenuPathString() {
        if (linkCount == 1)
            return "";
        return ";Add;Links";
    }

    public boolean handleTarget(ContextEditablePreset p, int total, int curr) throws Exception {
        ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
        int num = presets.length;
        HashSet done = new HashSet();
        for (int n = 0; n < num; n++) {
            if (!done.contains(presets[n])) {
                newLink(presets[n]);
                done.add(presets[n]);
            }
        }
        return false;
    }
}

