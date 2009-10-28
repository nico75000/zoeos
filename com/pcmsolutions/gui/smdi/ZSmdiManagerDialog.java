package com.pcmsolutions.gui.smdi;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.smdi.SMDIAgent;
import com.pcmsolutions.smdi.SmdiTarget;
import com.pcmsolutions.smdi.SmdiUnavailableException;
import com.pcmsolutions.smdi.TargetNotSMDIException;
import com.pcmsolutions.system.ScsiIdProvider;
import com.pcmsolutions.system.ZExternalDevice;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Mar-2003
 * Time: 23:00:21
 * To change this template use Options | File Templates.
 */
public class ZSmdiManagerDialog extends ZDialog implements ComponentListener {
    protected RowHeaderedAndSectionedTablePanel smdiPanel;
    protected SmdiManagerTable smdiManagerTable;
    protected JScrollPane scrollPane;

    public ZSmdiManagerDialog(Frame ownerFrame, boolean modal) throws HeadlessException {
        super(ownerFrame, "SMDI Manager", modal);

        JButton hideButt = new JButton(new AbstractAction("Hide") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        hideButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        hideButt.setToolTipText("Hide SMDI Manager");

        getRootPane().setDefaultButton(hideButt);

        JButton cButt = new JButton(new AbstractAction("Couple with Midi Device") {
            public void actionPerformed(ActionEvent e) {
                int sr = smdiManagerTable.getRowHeader().getSelectedRow();
                if (sr < 0) {
                    JOptionPane.showMessageDialog(ZSmdiManagerDialog.this, "Select a SMDI device first", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Object val = smdiManagerTable.getRowHeader().getValueAt(sr, 0);
                if (val == null || !(val instanceof SmdiTarget)) {
                    JOptionPane.showMessageDialog(ZSmdiManagerDialog.this, "Not a SMDI target", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                SmdiTarget st = ((SmdiTarget) val);
                List l = Zoeos.getInstance().getDeviceManager().getRunningList();
                if (l.size() > 0) {
                    int res = JOptionPane.showOptionDialog(ZSmdiManagerDialog.this, "Choose a device for the coupling:", "Choose Device", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, l.toArray(), l.get(0));
                    if (res != JOptionPane.CLOSED_OPTION && res != JOptionPane.CANCEL_OPTION)
                        if (l.get(res) instanceof ZExternalDevice) {
                            ZExternalDevice zed = (ZExternalDevice) l.get(res);
                            try {
                                if (zed instanceof ScsiIdProvider && ((ScsiIdProvider) zed).getScsiId() != st.getSCSI_Id())
                                    if (!UserMessaging.askYesNo("The Midi device you have chosen is not reporting the same SCSI Id as the SMDI device you have selected. Continue anyway?"))
                                        return;
                            } catch (ParameterException e1) {
                                e1.printStackTrace();
                            } catch (DeviceException e1) {
                                e1.printStackTrace();
                            }
                            try {
                                SMDIAgent.setSmdiTargetCoupling(st.getHA_Id(), st.getSCSI_Id(), ((ZExternalDevice) l.get(res)).getDeviceIdentityMessage());
                            } catch (TargetNotSMDIException e1) {
                                e1.printStackTrace();
                            } catch (SmdiUnavailableException e1) {
                                e1.printStackTrace();
                            }
                        }
                } else
                    UserMessaging.showError(ZSmdiManagerDialog.this, "No midi devices available");
            }
        });
        cButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        cButt.setToolTipText("Couple the selected SMDI Device to a Midi Device");

        JButton ccButt = new JButton(new AbstractAction("Clear All Couplings") {
            public void actionPerformed(ActionEvent e) {
                try {
                    SMDIAgent.clearCouplings();
                } catch (SmdiUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        });
        ccButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        ccButt.setToolTipText("Clear all the SMDI to Midi device couplings");


        JPanel bottomPanel = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }

        };
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(hideButt);
        bottomPanel.add(cButt);
        bottomPanel.add(ccButt);

        AbstractAction ract = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                if (e.getSource() instanceof Component) {
                    ((Component) e.getSource()).setEnabled(false);
                    try {
                        Zoeos.getInstance().getSystemQ().getPostableTicket(new TicketRunnable() {
                            public void run() throws Exception {
                                try {
                                    try {
                                        SMDIAgent.refresh();
                                    } catch (SmdiUnavailableException e1) {
                                        UserMessaging.showInfo("SMDI unavailable");
                                    }
                                } finally {
                                }
                            }
                        }, "refreshSMDI").post(new Callback() {
                            public void result(Exception e1, boolean wasCancelled) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        ((Component) e.getSource()).setEnabled(true);
                                    }
                                });
                            }
                        });
                    } catch (ResourceUnavailableException e1) {
                        e1.printStackTrace();
                        ((Component) e.getSource()).setEnabled(true);
                    }
                }
            }
        };
        ract.putValue("tip", "Refresh SMDI");


        smdiManagerTable = new SmdiManagerTable();
        smdiPanel = new RowHeaderedAndSectionedTablePanel().init(smdiManagerTable, null, null, ract);

        scrollPane = new JScrollPane(smdiPanel);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        smdiPanel.addComponentListener(this);

        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        //ZUtilities.applyHideButton(this, false, false);
        smdiManagerTable.addComponentListener(this);
        adjustSize();
    }

    public void dispose() {
        super.dispose();
    }

    private void adjustSize() {
        //scrollPane.getViewport().setPreferredSize(scrollPane.getViewport().getView().getPreferredSize());
        // scrollPane.setViewport(scrollPane.getViewport());

        pack();
        Dimension d = getSize();

        if (d.getHeight() > 400)
            setSize(new Dimension((int) d.getWidth(), 400));
    }

    public void componentResized(ComponentEvent e) {
        adjustSize();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }
}
