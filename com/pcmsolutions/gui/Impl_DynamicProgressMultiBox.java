/*
 * ProgressMultiBox.java
 *
 * Created on February 11, 2003, 10:32 AM
 */

package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author  pmeehan
 */
public class Impl_DynamicProgressMultiBox extends Impl_ProgressMultiBox implements DynamicProgressMultiBox {
    protected int holdTime;

    public Impl_DynamicProgressMultiBox(JFrame frame, String title, boolean modal) {
        this(frame, title, modal, null, 500);
    }

    public Impl_DynamicProgressMultiBox(JFrame frame, String title, boolean modal, Component relativeTo, int holdTime) {
        super(frame, title, modal, relativeTo);
        this.holdTime = holdTime;
    }

    public void killElement(final Object e) {
        final Runnable run = new Runnable() {
            public void run() {
                ProgressElement pe = (ProgressElement) progressElements.remove(e);
                //pe.updateTitle(PROGRESS_DONE_TITLE);
                if (pe != null) {
                    pe.setIndeterminate(false); // in case it is indeterminate
                    pe.maxBar();
                    KillThread t = new KillThread(pe);
                    t.start();
                }
            }
        };
        //   if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /* else
             try {
                 SwingUtilities.invokeAndWait(run);
             } catch (InterruptedException e1) {
                 e1.printStackTrace();
             } catch (InvocationTargetException e1) {
                 e1.printStackTrace();
             }
          */
    }

    private class KillThread extends Thread {
        private ProgressElement pe;

        private KillThread(ProgressElement pe) {
            this.pe = pe;
        }

        public void run() {
            do
                try {
                    sleep(holdTime);
                } catch (Exception e) {
                }
            while (progressElements.size() > 0);

            final Runnable run = new Runnable() {
                public void run() {
                    //progressDialog.setVisible(true);
                    dlgContents.remove(pe);
                    progressDialog.pack();
                    if (progressElements.size() == 0)
                        progressDialog.setVisible(false);
                }
            };
            //   if (SwingUtilities.isEventDispatchThread())
            SwingUtilities.invokeLater(run);
            /* else
                 try {
                     SwingUtilities.invokeAndWait(run);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 } catch (InvocationTargetException e1) {
                     e1.printStackTrace();
                 }
                 */
        }
    }
}
