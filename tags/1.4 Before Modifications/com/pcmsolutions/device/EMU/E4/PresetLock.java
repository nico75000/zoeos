package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.threads.ZThread;
import com.pcmsolutions.util.RWLock;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 22-Mar-2004
 * Time: 15:14:47
 */
public class PresetLock extends RWLock implements Serializable {

    public void refresh() throws IllegalArgumentException {
        super.write();
    }
    public void write() throws IllegalArgumentException {
        if (!(Thread.currentThread() instanceof ZThread)) {
            Thread.dumpStack();            
            throw new IllegalArgumentException("must be Impl_ZThread to modify a preset");
        }
        super.write();
    }
}
