package com.pcmsolutions.gui;

import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.util.ScreenUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 14-Mar-2003
 * Time: 02:18:00
 * To change this template use Options | File Templates.
 */
public class ZCommandInitializer implements ActionListener {
    public static final int CANCELLED = -1;
    public static final int CANCELLED_ALL = -2;
    public static final int FAILED = -3;
    public static final int COMPLETED = 0;

    private static int summaryFieldLength = 80;

    public Rectangle dlgRect;

    private ZCommand command;
    private Object[] args;
    private HashMap comp2arg;
    private JDialog dlg;
    private JLabel summaryLabel;

    private JButton ok = null;
    private JButton cancel = null;
    private JButton cancelAll = null;
    private int returnValue;
    //private double progressRatio;

    public ZCommandInitializer() {
    }

    public int run(ZCommand command, JFrame ownerFrame, Component relativeTo) throws Exception {
        return run(command, ownerFrame, relativeTo, 1, 1);
    }

    public int run(ZCommand command, JFrame ownerFrame, Component relativeTo, int numCmds, int iteration) throws Exception {
        this.command = command;
        comp2arg = new HashMap();
        returnValue = CANCELLED;
        comp2arg.clear();
        args = new Object[command.getNumberOfArguments()];

        constructDialog(ownerFrame, command, relativeTo, numCmds, iteration);

        if (ownerFrame != null)
            dlg.setLocation(ScreenUtilities.centreRect(ownerFrame.getBounds(), dlg.getBounds()));

        if (dlgRect != null)
            dlg.setBounds(dlgRect);

        dlg.show();

        return returnValue;
    }

    public void setDlgRect(Rectangle r) {
        dlgRect = r;
    }

    private void constructDialog(JFrame ownerFrame, ZCommand command, Component relativeTo, int numCmds, int iteration) throws Exception {
        dlg = new JDialog(ownerFrame, command.getDescriptiveString(), true);

        dlg.setLocationRelativeTo(relativeTo);
        Container dlgPane = dlg.getContentPane();
        dlgPane.setLayout(new BoxLayout(dlgPane, BoxLayout.Y_AXIS));

        constructArgumentFields(command, dlgPane);
        constructButtons(numCmds, dlgPane);
        constructSummary(command, dlgPane);
        if (numCmds > 1)
            constructProgressBar(dlgPane, numCmds, iteration);
        dlg.setResizable(false);
        dlg.pack();
    }

    private void constructProgressBar(Container dlgPane, int numCmds, int iteration) {
        JProgressBar pb = new JProgressBar(0, numCmds);
        JPanel pbPanel = new JPanel(new FlowLayout());
        pb.setValue((iteration + 1));
        pbPanel.add(new JLabel("Command " + (iteration + 1) + " of " + numCmds));
        pbPanel.add(pb);
        dlgPane.add(pbPanel);
    }

    private void constructSummary(ZCommand command, Container dlgPane) {
        summaryLabel = new JLabel();
        JPanel summaryPanel = new JPanel(new FlowLayout());
        summaryLabel.setText(ZUtilities.makeExactLengthString(command.getSummaryString(args), summaryFieldLength));
        summaryPanel.add(summaryLabel);
        dlgPane.add(summaryPanel);
    }

    private void constructButtons(int numCmds, Container dlgPane) {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        ok = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                grabArgs();
                performCommand();
            }
        });
        //run.addActionListener(this);
        //run.setMnemonic(KeyEvent.VK_R);

        cancel = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                returnValue = CANCELLED;
                exitDlg();
            }
        });
        //cancel.addActionListener(this);
        cancel.setMnemonic(KeyEvent.VK_C);

        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        cancelAll = null;
        if (numCmds > 1) {
            cancelAll = new JButton(new AbstractAction("Cancel All") {
                public void actionPerformed(ActionEvent e) {
                    returnValue = CANCELLED_ALL;
                    exitDlg();
                }
            });
            // cancelAll.addActionListener(this);
            cancelAll.setMnemonic(KeyEvent.VK_A);
            buttonPanel.add(cancelAll);
        }

        dlgPane.add(buttonPanel);

        dlg.getRootPane().setDefaultButton(ok);
        dlg.getRootPane().registerKeyboardAction(ok.getAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dlg.getRootPane().registerKeyboardAction(cancel.getAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dlg.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                //ok.requestFocus();
            }
        });
    }

    private void constructArgumentFields(ZCommand command, Container dlgPane) throws Exception {
        Box pb;
        JComponent comp;
        int na = command.getNumberOfArguments();
        String[] aps = command.getArgumentPresentationStrings();
        String[] ads = command.getArgumentDescriptiveStrings();
        for (int n = 0; n < na; n++) {
            pb = Box.createHorizontalBox();
            //pb.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel l = new JLabel(aps[n]);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            pb.add(l);
            try {
                comp = command.getComponentForArgument(n);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dlg, e.getMessage(), "Problem", JOptionPane.ERROR_MESSAGE);
                throw e;
            }
            comp.setAlignmentX(Component.RIGHT_ALIGNMENT);
            ToolTipManager.sharedInstance().registerComponent(comp);
            comp.setToolTipText(ads[n]);
            // PM: 08-08-03 - not properly tested
            if (comp != null) {
                comp2arg.put(comp, IntPool.get(n));

                if (comp instanceof JTextField) {
                    args[n] = ((JTextField) comp).getText();
                    ((JTextField) comp).addActionListener(this);
                } else if (comp instanceof JComboBox) {
                    args[n] = ((JComboBox) comp).getSelectedItem();
                    ((JComboBox) comp).addActionListener(this);
                } else if (comp instanceof JCheckBox) {
                    args[n] = new Boolean(((JCheckBox) comp).isSelected());
                    ((JCheckBox) comp).addActionListener(this);
                }

                pb.add(comp);
                dlgPane.add(pb);
            }
        }
    }

    private void exitDlg() {
        for (Iterator i = comp2arg.keySet().iterator(); i.hasNext();) {
            ToolTipManager.sharedInstance().unregisterComponent((JComponent) i.next());
        }
        dlgRect = dlg.getBounds();
        dlg.dispose();
    }

    //  COMPLETED, CANCELLED, CANCELLED_ALL

    private void disableElements() {
        if (ok != null)
            ok.setEnabled(false);
        if (cancel != null)
            cancel.setEnabled(false);
        if (cancelAll != null)
            cancelAll.setEnabled(false);

        for (Iterator i = comp2arg.keySet().iterator(); i.hasNext();) {
            ((JComponent) i.next()).setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent e) {
        grabArgs();
        JComponent comp = (JComponent) e.getSource();
        summaryLabel.setText(ZUtilities.makeExactLengthString(command.getSummaryString(args), summaryFieldLength));
        if (comp instanceof JTextField)
            performCommand();
    }


    private void grabArgs() {
        JComponent comp;
        for (Iterator i = comp2arg.keySet().iterator(); i.hasNext();) {
            comp = (JComponent) i.next();
            if (comp instanceof JTextField)
                args[((Integer) comp2arg.get(comp)).intValue()] = ((JTextField) comp).getText();
            else if (comp instanceof JComboBox)
                args[((Integer) comp2arg.get(comp)).intValue()] = ((JComboBox) comp).getSelectedItem();
            else if (comp instanceof JCheckBox)
                args[((Integer) comp2arg.get(comp)).intValue()] = new Boolean(((JCheckBox) comp).isSelected());
        }
    }

    private void performCommand() {
        summaryLabel.setText("Running...Please Wait");

        disableElements();
        final Exception[] fe = new Exception[1];
        Thread worker = new Thread("ZCommandInitializer perform command") {
            public void run() {
                try {
                    tryExecute(command, args);
                    //command.execute(this, args);
                    returnValue = COMPLETED;
                } catch (Exception e) {
                    returnValue = FAILED;
                    fe[0] = e;
                }
                // try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (returnValue == FAILED)
                            JOptionPane.showMessageDialog(dlg, fe[0].getMessage(), "Command Failed", JOptionPane.ERROR_MESSAGE);
                        exitDlg();
                    }
                });
                //    } catch (InterruptedException e) {
                //      e.printStackTrace();
                //   } catch (InvocationTargetException e) {
                //     e.printStackTrace();
                //  }
            };
        };
        worker.start();
    }

    private void tryExecute(ZCommand command, Object[] args) throws CommandFailedException {
        String vs = command.getVerificationString();
        if (vs != null) {
            if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), vs, command.getPresentationString(), JOptionPane.YES_NO_OPTION) != 0)
                return;
        }
        command.execute(this, args);
    }
}
