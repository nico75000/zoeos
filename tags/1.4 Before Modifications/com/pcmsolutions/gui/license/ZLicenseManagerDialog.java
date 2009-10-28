package com.pcmsolutions.gui.license;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.license.InvalidLicenseKeyException;
import com.pcmsolutions.license.LicenseKeyManager;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Mar-2003
 * Time: 23:00:21
 * To change this template use Options | File Templates.
 */
public class ZLicenseManagerDialog extends ZDialog implements ComponentListener, LicenseKeyManager.LicenseKeyListener {
    protected RowHeaderedAndSectionedTablePanel licensePanel;
    protected LicenseManagerTable licenseManagerTable;
    protected JScrollPane scrollPane;

    private static final String PREF_licenseManagerMaxHeight = "licenseManagerMaxHeight";

    public ZLicenseManagerDialog(Frame ownerFrame, boolean modal) throws HeadlessException {
        super(ownerFrame, "License Key Manager", modal);

        JButton hideButt = new JButton(new AbstractAction("Hide") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        hideButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        hideButt.setToolTipText("Hide License Key Manager");

        getRootPane().setDefaultButton(hideButt);

        JButton akButt = new JButton(new AbstractAction("Add license") {
            public void actionPerformed(ActionEvent e) {
                String s = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Enter or paste (ctrl-v) new license key here: ", "New License Key", JOptionPane.QUESTION_MESSAGE, null, null, "");
                if (s != null && !s.equals(""))
                    try {
                        LicenseKeyManager.addLicenseKey((LicenseKeyManager.parseKey(s)));
                    } catch (InvalidLicenseKeyException e1) {
                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Invalid license key!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
            }
        });
        akButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        akButt.setToolTipText("Add a new license key");

        /*  JButton dcButt = new JButton(new AbstractAction("Decouple from Midi Device") {
              public void actionPerformed(ActionEvent e) {
                  SMDIAgent.setSmdiTargetCoupling();
              }
          });
          dcButt.setAlignmentX(Component.LEFT_ALIGNMENT);
          dcButt.setToolTipText("Decouple the selected SMDI Device from a Midi Device");
          */

        JButton rkButt = new JButton(new AbstractAction("Remove license") {
            public void actionPerformed(ActionEvent e) {
                try {
                    int sr = licenseManagerTable.getRowHeader().getSelectedRow();
                    if (sr < 0) {
                        JOptionPane.showMessageDialog(ZLicenseManagerDialog.this, "Select a license first", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Are you sure you want to remove this license key? ", "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0)
                        LicenseKeyManager.removeLicenseKey(licenseManagerTable.getModel().getValueAt(sr, 5).toString());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        rkButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        rkButt.setToolTipText("Remove selected license key");


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
        bottomPanel.add(akButt);
        bottomPanel.add(rkButt);

        AbstractAction ract = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                if (e.getSource() instanceof Component) {
                    ((Component) e.getSource()).setEnabled(false);
                    try {
                        Zoeos.getInstance().getSystemQ().getPostableTicket(new TicketRunnable() {
                            public void run() throws Exception {
                                try {
                                    LicenseKeyManager.refreshLicenseKeys();
                                } finally {
                                }
                            }
                        }, "refreshLicenseKeys").post(new Callback() {
                            public void result(Exception e1, boolean wasCancelled) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        ((Component) e.getSource()).setEnabled(true);
                                    }
                                });
                            }
                        });
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        ((Component) e.getSource()).setEnabled(true);
                    }
                }
            }
        };
        ract.putValue("tip", "Refresh license keys");


        licenseManagerTable = new LicenseManagerTable();
        licensePanel = new RowHeaderedAndSectionedTablePanel().init(licenseManagerTable, null, null, ract);

        scrollPane = new JScrollPane(licensePanel);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        licenseManagerTable.addComponentListener(this);

        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        adjustSize();
        LicenseKeyManager.addLicenseKeyListener(this);
    }

    public void dispose() {
        super.dispose();
        LicenseKeyManager.removeLicenseKeyListener(this);
    }

    private void adjustSize() {
        pack();
        Dimension d = getSize();
        int maxHeight = Preferences.userNodeForPackage(this.getClass()).getInt(PREF_licenseManagerMaxHeight, 400);

        if (d.getHeight() > maxHeight)
            setSize(new Dimension((int) d.getWidth(), maxHeight));
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

    public void licenseKeysChanged() {
    }
}
