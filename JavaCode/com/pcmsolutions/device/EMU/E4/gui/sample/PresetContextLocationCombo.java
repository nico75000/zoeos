package com.pcmsolutions.device.EMU.E4.gui.sample;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.database.gui.ContextLocationCombo;
import com.pcmsolutions.system.IntPool;

/**
 * User: paulmeehan
 * Date: 05-Sep-2004
 * Time: 19:38:55
 */
public class PresetContextLocationCombo extends ContextLocationCombo {
    public void init(PresetContext pc) throws DeviceException {
        super.init(pc, IntPool.zero, IntPool.get(pc.databaseSize()));
    }
}
