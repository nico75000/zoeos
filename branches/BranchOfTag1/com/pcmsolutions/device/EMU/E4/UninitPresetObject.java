package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.PresetNameChangeEvent;
import com.pcmsolutions.system.Nameable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 30-Mar-2003
 * Time: 03:31:44
 * To change this template use Options | File Templates.
 */
class UninitPresetObject implements Nameable, RemoteObjectStates, Serializable {
    private transient static final String defUninitializedName = "+pending+";

    private volatile int state = STATE_PENDING;
    private String name = defUninitializedName;
    private Integer preset;
    private transient PresetEventHandler peh;

    public UninitPresetObject(Integer preset, String uninitializedName) {
        this.preset = preset;
        this.name = uninitializedName;
    }

    public UninitPresetObject(Integer preset) {
        this.preset = preset;
    }

    public synchronized void setPEH(PresetEventHandler peh) {
        this.peh = peh;
    }

    public synchronized void setName(String name) {
        setName(name, true);
    }

    public synchronized void setName(String name, boolean applyNamedState) {
        this.name = name;
        // if (name.trim().equals(DeviceContext.EMPTY_PRESET))
        //   state = STATE_EMPTY;
        //else
        if (applyNamedState)
            state = STATE_NAMED;

        if (peh != null)
            peh.postPresetEvent(new PresetNameChangeEvent(this, preset));
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void markInitializing() {
        state = STATE_INITIALIZING;
    }

    public synchronized void markPending() {
        state = STATE_PENDING;
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized String toString() {
        return name;
    }
}

