package com.pcmsolutions.device.EMU.E4.events.multimode;

import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeListener;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 16:53:08
 * To change this template use Options | File Templates.
 */
public class MultiModeChannelChangedEvent extends MultiModeEvent {
    private Integer channel;

    public MultiModeChannelChangedEvent(Object source, MultiModeContext mmc, Integer channel) {
        super(source, mmc);
        this.channel = channel;
    }

    public Integer getChannel() {
        return channel;
    }

    public String toString() {
        return "MultiModeChannelChangedEvent";
    }

    public void fire(MultiModeListener mml) {
        if (mml != null)
            mml.mmChannelChanged(this);
    }
}
