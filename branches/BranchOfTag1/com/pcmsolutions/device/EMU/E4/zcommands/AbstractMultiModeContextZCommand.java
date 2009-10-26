package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:59:51
 * To change this template use Options | File Templates.
 */
public abstract class AbstractMultiModeContextZCommand extends AbstractZCommand implements ZCommand, E4MultiModeContextZCommandMarker {
    protected AbstractMultiModeContextZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(MultiModeContext.class, presString, descString, argPresStrings, argDescStrings);
    }

    MultiModeContext getTarget() {
        return (MultiModeContext) target;
    }
}
