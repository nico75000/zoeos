package com.pcmsolutions.device.EMU.E4.events.preset.requests;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

/**
 * User: paulmeehan
 * Date: 26-Mar-2004
 * Time: 19:09:27
 */
public class PresetNameRequestEvent extends PresetRequestEvent<String> {
    public PresetNameRequestEvent(Object source, Integer preset) {
        super(source, preset);
    }
}
