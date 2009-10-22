package com.pcmsolutions.device.EMU.E4.events.multimode;

import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeListener;

public class MultiModeEvent extends java.util.EventObject {

    private MultiModeContext mmc;

    public MultiModeEvent(Object source, MultiModeContext mmc) {
        super(source);
        this.mmc = mmc;
    }

    public String toString() {
        return "MultiModetEvent";
    }

    public MultiModeContext getMultiModeContext() {
        return mmc;
    }

    public void fire(MultiModeListener mml) {
        throw new IllegalArgumentException("Cannot fire a basic MultiModeEvent.");
    }
}
