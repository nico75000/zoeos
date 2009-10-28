package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableZoneZCommandMarker;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractReadableZoneZMTCommand extends AbstractZMTCommand<ReadablePreset.ReadableVoice.ReadableZone> implements E4ReadableZoneZCommandMarker {
    public String getPresentationCategory() {
        return "Zone";
    }
}
