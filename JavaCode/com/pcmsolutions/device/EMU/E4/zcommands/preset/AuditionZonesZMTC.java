package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AuditionZonesZMTC extends AbstractReadableZoneZMTCommand {

    boolean stepped;

    public AuditionZonesZMTC() {
        this(false);
    }

    protected AuditionZonesZMTC(boolean stepped) {
        this.stepped = stepped;
    }

    public ZMTCommand getNextMode() {
        if (!stepped)
            return new AuditionZonesZMTC(true);
        else
            return null;
    }
     public String getPresentationCategory() {
        return "Audition";
    }
    public String getPresentationString() {
        if (stepped)
            return "AudS";
        else
            return "Aud";
    }

    public String getDescriptiveString() {
        if (stepped)
            return "Audition zone samples individually in sequenece";
        else
            return "Audition zone samples";
    }

    public boolean handleTarget(ReadablePreset.ReadableVoice.ReadableZone readableZone, int total, int curr) throws Exception {
        ReadablePreset.ReadableVoice.ReadableZone[] zones = getTargets().toArray(new ReadablePreset.ReadableVoice.ReadableZone[numTargets()]);
        zones[0].getPreset().getDeviceContext().getDefaultPresetContext().auditionSamples(PresetContextMacros.extractSampleIndexes(zones), stepped).post();
        return false;
    }

    public int getMinNumTargets() {
        if (stepped)
            return 2;
        else
            return 1;
    }

    public int getMaxNumTargets() {
        return (stepped ? Integer.MAX_VALUE : 128);
    }
}

