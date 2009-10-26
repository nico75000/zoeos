package com.pcmsolutions.system.callback;

import com.pcmsolutions.gui.UserMessaging;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 22-Aug-2004
 * Time: 07:17:08
 */
public final class ShowCommandFailedCallback implements Callback {
    public static final ShowCommandFailedCallback INSTANCE = new ShowCommandFailedCallback();

    private ShowCommandFailedCallback() {
    }

    public void result(final Exception e, boolean wasCancelled) {
        if (e != null && !wasCancelled)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    UserMessaging.showCommandFailed(e.getMessage());
                }
            });
    }
}
