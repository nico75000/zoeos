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
public class EraseBankZC extends AbstractDeviceContextZCommand {

    public EraseBankZC() {
        super("Erase Bank", "Erase all RAM data on this device", null, null);
    }

    public int getMnemonic() {
        return KeyEvent.VK_E;
    }

    // if returns null no verification of command required
    public String getVerificationString() {
        return "Are you sure you want to erase the entire user bank? This operation is extremely destructive and will destroy all presets and samples in user memory.";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();
        try {
            getTarget().eraseBank();
        } catch (ZDeviceNotRunningException e) {
            throw new CommandFailedException("Device not running");
        }
    }

}
