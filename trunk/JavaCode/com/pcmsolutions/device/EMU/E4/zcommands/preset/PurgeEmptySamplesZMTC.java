package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class PurgeEmptySamplesZMTC extends AbstractContextEditablePresetZMTCommand {

    public String getPresentationString() {
        return "Empty samples";
    }

    public String getDescriptiveString() {
        return "Purge all voices and zones in preset that reference empty samples";
    }

    public String getMenuPathString() {
        return ";Purge";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        for (ContextEditablePreset p : getTargets())
        if ( !p.isEmpty())
            p.purgeEmpties();
        return false;
    }
}
