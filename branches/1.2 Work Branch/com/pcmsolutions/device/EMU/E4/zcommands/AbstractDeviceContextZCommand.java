package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.system.AbstractZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractDeviceContextZCommand extends AbstractZCommand implements E4DeviceZCommandMarker {
    protected AbstractDeviceContextZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(DeviceContext.class, presString, descString, argPresStrings, argDescStrings);
    }

    public DeviceContext getTarget() {
        return (DeviceContext) target;
    }
}
