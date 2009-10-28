package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MasterContextZCommandMarker;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:59:51
 * To change this template use Options | File Templates.
 */
public abstract class AbstractMasterContextZCommand extends AbstractZCommand<MasterContext> implements ZCommand, E4MasterContextZCommandMarker {
    public String getPresentationCategory() {
        return "Master";
    }
}
