package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.callback.ShowCommandFailedCallback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class EraseBankZC extends AbstractDeviceContextZCommand {

    public int getMnemonic() {
        return KeyEvent.VK_E;
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "Erase bank";
    }

    public String getDescriptiveString() {
        return "Erase all RAM data on this device";
    }

    public boolean handleTarget(final DeviceContext device, int total, int curr) throws Exception {
        try {
            if (UserMessaging.askYesNo("Are you sure you want to erase the entire user bank on " + device.getName() + "? This operation is extremely destructive and will destroy all presets and samples in user memory.", "Erase bank"))
                device.getQueues().zCommandQ().getPostableTicket(new TicketRunnable() {
                    public void run() throws Exception {
                        boolean clear = true;
                        try {
                            clear = device.getViewManager().hasWorkspaceElements() && UserMessaging.askYesNo("Clear device workspace first (recommended)?");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (clear) {
                            try {
                                device.getViewManager().clearDeviceWorkspace().send(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        device.eraseBank().post(ShowCommandFailedCallback.INSTANCE);
                    }
                }, "eraseBankZC").post(ShowCommandFailedCallback.INSTANCE);
        } catch (ResourceUnavailableException e) {
            throw new CommandFailedException(e.getMessage());
        }
        return true;
    }
}
