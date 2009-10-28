package com.pcmsolutions.device.EMU.E4.events.sample.requests;

import com.pcmsolutions.device.EMU.E4.remote.SampleHeader;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;

/**
 * User: paulmeehan
 * Date: 03-Sep-2004
 * Time: 17:28:49
 */
public interface SampleDumpResult {
    SampleDescriptor getDescriptor();
    boolean isEmpty();
    boolean onlyNameProvided();
    String getName();
}
