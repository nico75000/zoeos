package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializationStatusChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializationStatusChangedEvent;
import com.pcmsolutions.device.EMU.E4.remote.DumpMonitor;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:43:45
 * To change this template use Options | File Templates.
 */
class PresetInitializationMonitor implements DumpMonitor {
    final ManageableContentEventHandler ceh;
    final Integer preset;
    volatile double status = 0;

    public PresetInitializationMonitor(Integer preset, ManageableContentEventHandler ceh) {
        this.ceh = ceh;
        this.preset = preset;
    }

    public double getStatus() {
        return status;
    }

    public void setStatus(double status) {
        this.status = status;
        try {
            ceh.sendInternalEvent(new PresetInitializationStatusChangedEvent(this, preset, status));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
