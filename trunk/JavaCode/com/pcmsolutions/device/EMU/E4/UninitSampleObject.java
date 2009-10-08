package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.SampleNameChangeEvent;
import com.pcmsolutions.system.Nameable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 30-Mar-2003
 * Time: 03:31:44
 * To change this template use Options | File Templates.
 */
class UninitSampleObject implements Nameable, RemoteObjectStates, Serializable {
    private static final String defUninitializedName = "+pending+";
    private volatile int state = STATE_PENDING;
    private String name = defUninitializedName;
    private Integer sample;
    private transient SampleEventHandler seh;

    public UninitSampleObject(Integer sample, String uninitializedName) {
        this.sample = sample;
        this.name = uninitializedName;
    }

    public UninitSampleObject(Integer sample) {
        this.sample = sample;
    }

    public synchronized void setSEH(SampleEventHandler seh) {
        this.seh = seh;
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

        if (seh != null)
            seh.postSampleEvent(new SampleNameChangeEvent(this, sample));
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

