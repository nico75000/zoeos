/*
 * ZJFrame.java
 *
 * Created on February 11, 2003, 10:56 AM
 */

package com.pcmsolutions.gui;

import com.jidesoft.docking.DefaultDockableHolder;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;
import com.pcmsolutions.system.threads.ZDefaultThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 *
 * @author  pmeehan
 */
public class ZJFrame extends DefaultDockableHolder implements WindowListener {
    protected DynamicProgressMultiBox pmb;

    /** Creates a new instance of ZJFrame */
    public ZJFrame() {
        pmb = new Impl_DynamicProgressMultiBox(this, "Tasks", false);
        this.addWindowListener(this);
    }

    public void showTasks() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                pmb.show();
            }
        });
    }

    public void hideTasks() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                pmb.hide();
            }
        });
    }

    public void beginProgressElement(Object e, String title, int maximum) {
        pmb.newElement(e, title, maximum);
    }

    public void endProgressElement(Object e) {
        pmb.killElement(e);
    }

    public void updateProgressElement(Object e, int status) {
        pmb.updateElement(e, status);
    }

    public void setProgressElementIndeterminate(Object e, boolean b) {
        pmb.setElementIndeterminate(e, b);
    }

    public void updateProgressElementTitle(Object e, String title) {
        pmb.updateTitle(e, title);
    }

    public void updateProgressElement(Object e) {
        pmb.updateElement(e);
    }

    public void updateProgressElement(Object e, String title) {
        pmb.updateElement(e, title);
    }

    public ProgressMultiBox getCustomProgress(String title, Component relativeTo) {
        return new Impl_ProgressMultiBox(this, title, false, relativeTo);
    }

    public ProgressMultiBox getCustomProgress(String title) {
        return new Impl_ProgressMultiBox(this, title, false);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }


    private Thread revokeThread = null;
    public void windowIconified(WindowEvent e) {
        boolean revoked = ZoeosPreferences.ZPREF_releaseMidiOnMinimize.getValue();
        if (revoked) {
            if (revokeThread != null)
                while (revokeThread.isAlive())
                    try {
                        revokeThread.join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

            revokeThread = new ZDefaultThread("Revoke Thread") {
                public void run() {
                    FlashMsg.globalDisable = true;
                    Zoeos.getInstance().getDeviceManager().revokeDevicesNonThreaded();
                }
            };
            revokeThread.start();
        }
    }

    public void windowDeiconified(WindowEvent e) {
        if (revokeThread != null) {
            while (revokeThread.isAlive())
                try {
                    revokeThread.join();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            Zoeos.getInstance().getDeviceManager().unrevokeDevices();
            FlashMsg.globalDisable = false;
            revokeThread = null;
        }
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
