package com.pcmsolutions.device.EMU.E4.events.preset.requests;

import com.pcmsolutions.device.EMU.database.events.content.ContentRequestEvent;
import com.pcmsolutions.device.EMU.database.events.content.ContentListener;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;

/**
 * User: paulmeehan
 * Date: 03-Sep-2004
 * Time: 13:38:02
 */
public abstract class PresetRequestEvent<D extends Object> extends ContentRequestEvent<D>{
    protected PresetRequestEvent(Object source, Integer index) {
        super(source, index);
    }
}
