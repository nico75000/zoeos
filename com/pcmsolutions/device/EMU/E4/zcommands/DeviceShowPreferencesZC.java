package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.system.CommandFailedException;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class DeviceShowPreferencesZC extends AbstractDeviceContextZCommand {

    public DeviceShowPreferencesZC() {
        super("preferences", "Show the preference dialog for this device", null, null);
    }

    public int getMnemonic() {
        return KeyEvent.VK_P;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();
        // TODO re-implement this
        // getTarget().showPreferencesDialog();
    }
}
