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
public class RemoveLinkZMTC extends AbstractEditableLinkZMTCommand {

    public String getPresentationString() {
        return "Delete";
    }

    public String getDescriptiveString() {
        return "Delete Link";
    }

    public boolean handleTarget(ContextEditablePreset.EditableLink editableLink, int total, int curr) throws Exception {
        ContextEditablePreset.EditableLink[] links = getTargets().toArray(new ContextEditablePreset.EditableLink[numTargets()]);
        int num = links.length;
        Arrays.sort(links);
        for (int n = num - 1; n >= 0; n--)
            links[n].removeLink();
        return false;
    }
}

