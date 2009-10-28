/*
 * ZJFrame.java
 *
 * Created on February 11, 2003, 10:56 AM
 */

package com.pcmsolutions.gui;

import com.jidesoft.docking.DefaultDockableHolder;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pmeehan
 */
public class ZJFrame extends DefaultDockableHolder implements WindowListener {
    // protected DynamicProgressMultiBox pmb;

    /**
     * Creates a new instance of ZJFrame
     */
    public ZJFrame() {
        // pmb = new Impl_DynamicProgressMultiBox(this, "Tasks", false);
        this.addWindowListener(this);
        // this.getRootPane().setDoubleBuffered(false);
        DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                //  System.out.println("FOCUS: " + (evt.getOldValue() == null ? null : evt.getOldValue().getClass()) + " -> " + (evt.getNewValue() == null ? null : evt.getNewValue().getClass()));
            }
        });
    }

    /* public void showTasks() {
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
      */
    /*
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
    */
    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }


    public void windowIconified(WindowEvent e) {
        boolean revoked = ZoeosPreferences.ZPREF_releaseMidiOnMinimize.getValue();
        if (revoked) {
            if (!ProgressCallbackTree.hasActiveTasks())
                try {
                    FlashMsg.globalDisable = true;
                    Zoeos.getInstance().getDeviceManager().revokeDevices("ZoeOS window iconified").post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
        }
    }

    public void windowDeiconified(WindowEvent e) {
        try {
            FlashMsg.globalDisable = false;
            Zoeos.getInstance().getDeviceManager().unrevokeDevices().post();
        } catch (ResourceUnavailableException e1) {
            e1.printStackTrace();
        }
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
