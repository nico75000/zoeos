package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MultiModeContextZCommandMarker;
import com.pcmsolutions.system.AbstractZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:59:51
 * To change this template use Options | File Templates.
 */
public abstract class AbstractMultiModeContextZCommand extends AbstractZCommand<MultiModeContext> implements E4MultiModeContextZCommandMarker {
    public String getPresentationCategory() {
        return "Multimode";
    }
}
