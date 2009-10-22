package com.pcmsolutions.device.EMU.E4.events.preset.requests;

import java.io.ByteArrayInputStream;

/**
 * User: paulmeehan
 * Date: 26-Mar-2004
 * Time: 19:09:27
 */
public class PresetDumpRequestEvent extends PresetRequestEvent<PresetDumpResult> {
    public PresetDumpRequestEvent(Object source, Integer preset) {
        super(source, preset);
    }
}
