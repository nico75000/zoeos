package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class NewVoiceZoneZMTC extends AbstractEditableVoiceZMTCommand {
    private int zoneCount;

    public NewVoiceZoneZMTC() {
        this(1);
    }

    public NewVoiceZoneZMTC(int zoneCount) {
        this.zoneCount = zoneCount;
    }

    public boolean isSuitableInToolbar() {
        if (zoneCount == 1)
            return true;
        return false;
    }

    public String getPresentationString() {
        return (zoneCount == 1 ? "AddZ" : zoneCount + " zones");
    }

    public String getDescriptiveString() {
        return (zoneCount == 1 ? "Add 1 new zone to voice" : "Add " + zoneCount + " new zones");
    }

    public ZMTCommand getNextMode() {
        if (zoneCount == 8)
            return null;
        return new NewVoiceZoneZMTC(zoneCount + 1);
    }

    public String getMenuPathString() {
        if (zoneCount == 1)
            return "";
        return ";Add zones";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        int num = voices.length;
        HashSet done = new HashSet();
        for (int n = 0; n < num; n++) {
            if (!done.contains(voices[n])) {
                voices[n].newZones(IntPool.get(zoneCount));
                done.add(voices[n]);
            }
        }
        return false;
    }
}

