package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractDeviceContextZMTCommand extends AbstractZMTCommand implements E4DeviceZCommandMarker {
    protected AbstractDeviceContextZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(DeviceContext.class, presString, descString, argPresStrings, argDescStrings);
    }

    DeviceContext getTarget() {
        return (DeviceContext) target;
    }

    public DeviceContext[] getTargets() {
        if (targets == null)
            return new DeviceContext[0];
        int num = targets.length;
        DeviceContext[] devices = new DeviceContext[num];

        for (int n = 0; n < num; n++) {
            devices[n] = (DeviceContext) targets[n];
        }
        return devices;
    }
}
