package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.PresetInitializationStatusChangedEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:43:45
 * To change this template use Options | File Templates.
 */
class PresetInitializationMonitor {
    private PresetEventHandler peh;
    private Integer preset;

    private volatile double status = RemoteObjectStates.STATUS_INITIALIZED;

    public PresetInitializationMonitor(Integer preset, PresetEventHandler peh) {
        this.peh = peh;
        this.preset = preset;
    }

    public double getStatus() {
        return status;
    }

    public void setStatus(double status) {
        this.status = status;
        peh.postPresetEvent(new PresetInitializationStatusChangedEvent(this, preset, status));
    }
}
