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
        progressDialog.getContentPane().setLayout(new BoxLayout(progressDialog.getContentPane(), BoxLayout.Y_AXIS));
        dlgContents = progressDialog.getContentPane();
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

    protected class ProgressElement extends Box {
        String title;
        JProgressBar bar = new JProgressBar();
        JLabel label = new JLabel();
        JProgressBar b;
        Dimension initialLabelSize;

        private ProgressElement(String title, int maximum) {
            super(BoxLayout.X_AXIS);
            this.setAlignmentX(Component.RIGHT_ALIGNMENT);
            // bar.setAlignmentX(Component.RIGHT_ALIGNMENT);
            //label.setAlignmentX(Component.LEFT_ALIGNMENT);

            this.title = title;
            bar.setMaximum(maximum);
            Dimension barDimension = new Dimension(150, 20);
            bar.setMaximumSize(barDimension);
            bar.setPreferredSize(barDimension);
            bar.setMinimumSize(barDimension);
            //bar.setAlignmentX(Component.RIGHT_ALIGNMENT);

            label.setText(title);
            //label.setAlignmentX(Component.LEFT_ALIGNMENT);
            //label.setHorizontalAlignment(JLabel.LEFT);
            initialLabelSize = label.getPreferredSize();
            label.setMaximumSize(initialLabelSize);
            add(label);
            add(bar);

        }

        protected void maxBar() {
            bar.setValue(bar.getMaximum());
        }

        protected void setMaximum(int max) {
            bar.setMaximum(max);
        }

        protected void updateBar(final int status) {
            bar.setValue(status);
        }

        protected void updateTitle(final String title) {
            label.setText(title);
            label.setPreferredSize(initialLabelSize);
        }


        protected void setIndeterminate(boolean b) {
            bar.setIndeterminate(b);
        }

        protected void updateBar() {
            bar.setValue(bar.getValue() + 1);
        }
    }
}
