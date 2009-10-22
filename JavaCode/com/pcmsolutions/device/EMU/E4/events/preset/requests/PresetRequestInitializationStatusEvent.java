package com.pcmsolutions.device.EMU.E4.events.preset.requests;

import com.pcmsolutions.device.EMU.E4.events.preset.PresetEvent;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

/**
 * User: paulmeehan
 * Date: 26-Mar-2004
 * Time: 19:48:09
 */
public class PresetRequestInitializationStatusEvent extends PresetRequestEvent<Double> {
    public PresetRequestInitializationStatusEvent(Object source, Integer preset) {
        super(source, preset);
    }
}
