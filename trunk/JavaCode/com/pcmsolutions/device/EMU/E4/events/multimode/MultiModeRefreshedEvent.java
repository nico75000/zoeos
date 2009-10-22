package com.pcmsolutions.device.EMU.E4.events.multimode;

import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeListener;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 16:53:08
 * To change this template use Options | File Templates.
 */
public class MultiModeRefreshedEvent extends MultiModeEvent {

    public MultiModeRefreshedEvent(Object source, MultiModeContext mmc) {
        super(source, mmc);
    }

    public String toString() {
        return "MultiModeRefreshedEvent";
    }

    public void fire(MultiModeListener mml) {
        if (mml != null)
            mml.mmRefreshed(this);
    }
}
