/*
 * DeviceListener.java
 *
 * Created on January 27, 2003, 10:29 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.PresetDBAddEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetDBRemoveEvent;

/**
 *
 * @author  pmeehan
 */
public interface DeviceListener {
    public void presetDBAdded(PresetDBAddEvent ev);

    public void presetDBRemoved(PresetDBRemoveEvent ev);
}
