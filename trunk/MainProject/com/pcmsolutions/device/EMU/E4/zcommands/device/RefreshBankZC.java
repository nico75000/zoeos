package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.callback.ShowCommandFailedCallback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import java.awt.event.KeyEvent;

public class RefreshBankZC extends AbstractDeviceContextZCommand {

    public int getMnemonic() {
        return KeyEvent.VK_R;
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "Refresh bank";
    }

    public String getDescriptiveString() {
        return "Refresh all RAM data on this device";
    }

    public boolean handleTarget(final DeviceContext device, int total, int curr) throws Exception {
        try {
            if (UserMessaging.askYesNo("Are you sure you wish to refresh the bank on " + device.toString() + "? - this operation will require local initialization of all user presets and samples.", "Refresh bank"))
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
                        device.refreshBank(false).post(ShowCommandFailedCallback.INSTANCE);
                    }
                }, "refreshBankZC").post(ShowCommandFailedCallback.INSTANCE);
        } catch (ResourceUnavailableException e) {
            throw new CommandFailedException(e.getMessage());
        }
        return true;
    }
}
