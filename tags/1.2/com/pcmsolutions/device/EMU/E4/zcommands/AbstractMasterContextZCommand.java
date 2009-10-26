package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:59:51
 * To change this template use Options | File Templates.
 */
public abstract class AbstractMasterContextZCommand extends AbstractZCommand implements ZCommand, E4MasterContextZCommandMarker {
    //protected ContextReadablePreset cp = (ContextReadablePreset)target;

    protected AbstractMasterContextZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(MasterContext.class, presString, descString, argPresStrings, argDescStrings);
    }

    MasterContext getTarget() {
        return (MasterContext) target;
    }
}
