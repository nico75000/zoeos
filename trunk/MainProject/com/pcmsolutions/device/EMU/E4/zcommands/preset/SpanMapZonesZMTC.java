package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.system.IntervalServices;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class SpanMapZonesZMTC extends AbstractEditableZoneZMTCommand {
    public int getMinNumTargets() {
        return 2;
    }

    public String getPresentationString() {
        return "SpanMapZ";
    }

    public String getDescriptiveString() {
        return "Map zones on regular key intervals starting on low key of first zone and ending on high key of last zone";
    }

    public String getMenuPathString() {
        return ";Key Mapping";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice.EditableZone editableZone, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice.EditableZone[] zones = getTargets().toArray(new ContextEditablePreset.EditableVoice.EditableZone[numTargets()]);
        int low = zones[0].getZoneParams(new Integer[]{ID.lowKey})[0].intValue();
        int high = zones[zones.length - 1].getZoneParams(new Integer[]{ID.highKey})[0].intValue();
        PresetContextMacros.intervalMapZoneKeyWin(zones, new IntervalServices.Mapper.Spanning(low, high, zones.length));
        return false;
    }
}

