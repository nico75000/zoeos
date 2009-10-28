package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;

import java.util.Iterator;

public class CopyLinkZMTC extends AbstractEditableLinkZMTCommand {
    public String getPresentationString() {
        return "Copy";
    }

    public String getDescriptiveString() {
        return "Copy link";
    }

    public boolean handleTarget(ContextEditablePreset.EditableLink editableLink, int total, int curr) throws Exception {
        for (Iterator<ContextEditablePreset.EditableLink> i = getTargets().iterator(); i.hasNext();)
            i.next().copyLink();
        return false;
    }
}

