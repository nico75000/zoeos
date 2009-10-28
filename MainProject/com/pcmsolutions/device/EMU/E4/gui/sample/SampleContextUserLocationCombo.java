package com.pcmsolutions.device.EMU.E4.gui.sample;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.database.gui.ContextLocationCombo;
import com.pcmsolutions.system.IntPool;

/**
 * User: paulmeehan
 * Date: 05-Sep-2004
 * Time: 19:38:55
 */
public class SampleContextUserLocationCombo extends ContextLocationCombo {
    public void init(SampleContext sc) throws DeviceException {
        super.init(sc, IntPool.one, IntPool.get(DeviceContext.BASE_ROM_SAMPLE));
    }
}
