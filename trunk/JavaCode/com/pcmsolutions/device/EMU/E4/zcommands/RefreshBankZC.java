package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RefreshBankZC extends AbstractDeviceContextZCommand {

    public RefreshBankZC() {
        super("Refresh Bank", "Refresh all RAM data on this device", null, null);
    }

    public int getMnemonic() {
        return KeyEvent.VK_R;
    }

    // if returns null no verification of command required
    public String getVerificationString() {
        return "Are you sure you wish to refresh the bank? This operation will require remote initialization of all user presets and samples.";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();
        try {
            getTarget().refreshBank(false);
        } catch (ZDeviceNotRunningException e) {
            throw new CommandFailedException("Device not running");
        }
    }

}
