/*
 * ProgressMultiBoxX.java
 *
 * Created on February 14, 2003, 7:09 PM
 */

package com.pcmsolutions.gui;

import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.util.ScreenUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;


/**
 *
 * @author  pmeehan
 */

public class Impl_ProgressMultiBox implements ProgressMultiBox {
    protected Hashtable progressElements = new Hashtable();
    protected JDialog progressDialog;
    protected Container dlgContents;
    protected JFrame ownerFrame;

    protected boolean isShowable = true;

    public Impl_ProgressMultiBox(JFrame frame, String title, boolean modal) {
        this(frame, title, modal, null);
    }

    public Impl_ProgressMultiBox(JFrame frame, String title, boolean modal, Component relativeTo) {
        progressDialog = new ZDialog(frame, title, false);
        progressDialog.setResizable(false);
        progressDialog.setLocationRelativeTo(relativeTo);
        progressDialog.getContentPane().setLayout(new BorderLayout());
        dlgContents = new Box(BoxLayout.Y_AXIS);
       // JScrollPane sp = new JScrollPane(dlgContents);
        progressDialog.getContentPane().add(dlgContents, BorderLayout.CENTER);
        ZUtilities.applyHideButton(progressDialog, false, false);
        this.ownerFrame = frame;
    }

    public void newElement(final Object e, final String title, final int maximum) {
        final Runnable run = new Runnable() {
            public void run() {
                ProgressElement pe = (ProgressElement) progressElements.get(e);
                if (pe == null) {
                    pe = new ProgressElement(title, maximum);
                    progressElements.put(e, pe);
                    dlgContents.add(pe);
                } else {
                    pe.setMaximum(maximum);
                    pe.updateTitle(title);
                }
                progressDialog.pack();
                if (!progressDialog.isVisible() && isShowable) {
                    progressDialog.show();
                }
            }
        };

        //  if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /*  else
              try {
                  SwingUtilities.invokeAndWait(run);
              } catch (InterruptedException e1) {
                  e1.printStackTrace();
              } catch (InvocationTargetException e1) {
                  e1.printStackTrace();
              }
              */
    }

    public void updateElement(final Object e, final int status) {
        final Runnable run = new Runnable() {
            public void run() {
                ProgressElement pe = (ProgressElement) progressElements.get(e);
                if (pe != null)
                    pe.updateBar(status);
            }
        };

        //  if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /*    else
                try {
                    SwingUtilities.invokeAndWait(run);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                */
    }

    public void updateElement(final Object e) {
        final Runnable run = new Runnable() {
            public void run() {
                ProgressElement pe = (ProgressElement) progressElements.get(e);
                if (pe != null)
                    pe.updateBar();

            }
        };
        //    if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /*    else
                try {
                    SwingUtilities.invokeAndWait(run);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                */
    }

    public void setElementIndeterminate(final Object e, final boolean b) {
        final Runnable run = new Runnable() {
            public void run() {
                ProgressElement pe = (ProgressElement) progressElements.get(e);
                if (pe != null)
                    pe.setIndeterminate(b);

            }
        };
        //    if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /*    else
                try {
                    SwingUtilities.invokeAndWait(run);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                */
    }

    public void updateElement(final Object e, final String title) {
        final Runnable run = new Runnable() {
            public void run() {
                ProgressElement pe = (ProgressElement) progressElements.get(e);
                if (pe != null) {
                    pe.updateBar();
                    pe.updateTitle(title);
                }

            }
        };
        //   if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /*   else
               try {
                   SwingUtilities.invokeAndWait(run);
               } catch (InterruptedException e1) {
                   e1.printStackTrace();
               } catch (InvocationTargetException e1) {
                   e1.printStackTrace();
               }
               */
    }

    public void updateTitle(final Object e, final String title) {
        final Runnable run = new Runnable() {
            public void run() {
                ProgressElement pe = (ProgressElement) progressElements.get(e);
                if (pe != null)
                    pe.updateTitle(title);

            }
        };
        //     if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /*   else
               try {
                   SwingUtilities.invokeAndWait(run);
               } catch (InterruptedException e1) {
                   e1.printStackTrace();
               } catch (InvocationTargetException e1) {
                   e1.printStackTrace();
               }
               */
    }

    public void killElement(Object e) {

    }

    public void kill() {
        final Runnable run = new Runnable() {
            public void run() {
                progressDialog.setVisible(false);
            }
        };
        //    if (SwingUtilities.isEventDispatchThread())
        SwingUtilities.invokeLater(run);
        /*    else
                try {
                    SwingUtilities.invokeAndWait(run);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                */
    }

    public void centreAboutFrame() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (ownerFrame != null)
                    progressDialog.setLocation(ScreenUtilities.centreRect(ownerFrame.getBounds(), progressDialog.getBounds()));
            }
        });
    }

    public void show() {
        if (isShowable)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressDialog.show();
                }
            });
    }

    public void hide() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressDialog.hide();
            }
        });
    }

    public void setShowable(boolean s) {
        isShowable = s;
        if (s == false)
            this.hide();
    }

    public boolean getShowable() {
        return isShowable;
    }
}
