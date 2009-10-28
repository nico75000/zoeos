package com.pcmsolutions.device.EMU.E4.multimode;

import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeChannelChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeRefreshedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeChannelChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeRefreshedEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 16:48:37
 * To change this template use Options | File Templates.
 */
public interface MultiModeListener {
    public void mmChannelChanged(MultiModeChannelChangedEvent ev);

    public void mmRefreshed(MultiModeRefreshedEvent ev);
}
