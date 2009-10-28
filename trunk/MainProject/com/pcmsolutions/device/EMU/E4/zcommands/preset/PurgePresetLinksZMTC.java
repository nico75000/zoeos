package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class PurgePresetLinksZMTC extends AbstractContextEditablePresetZMTCommand {
    public String getPresentationString() {
        return "Links";
    }

    public String getDescriptiveString() {
        return "Purge all links in preset";
    }

    public String getMenuPathString() {
        return ";Purge";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        for (Iterator<ContextEditablePreset> i = getTargets().iterator(); i.hasNext();)
            i.next().purgeLinks();
        return false;
    }
}
