package com.pcmsolutions.device.EMU.E4.events.sample.requests;

import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentRequestEvent;

/**
 * User: paulmeehan
 * Date: 03-Sep-2004
 * Time: 14:26:49
 */
public abstract class SampleRequestEvent<D extends Object> extends ContentRequestEvent<D>{
    protected SampleRequestEvent(Object source, Integer index) {
        super(source, index);
    }
}

