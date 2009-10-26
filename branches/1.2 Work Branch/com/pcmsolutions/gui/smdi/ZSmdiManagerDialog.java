package com.pcmsolutions.gui.smdi;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.smdi.SMDIAgent;
import com.pcmsolutions.smdi.SmdiTarget;
import com.pcmsolutions.smdi.SmdiUnavailableException;
import com.pcmsolutions.smdi.TargetNotSMDIException;
import com.pcmsolutions.system.ZExternalDevice;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.threads.ZDefaultThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Mar-2003
 * Time: 23:00:21
 * To change this template use Options | File Templates.
 */
public class ZSmdiManagerDialog extends ZDialog implements SMDIAgent.SmdiListener, ComponentListener {
    protected RowHeaderedAndSectionedTablePanel smdiPanel;
    protected SmdiManagerTable smdiManagerTable;
    protected JScrollPane scrollPane;

    private static final String PREF_smdiManagerMaxHeight = "smdiManagerMaxHeight";

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
                    JOptionPane.showMessageDialog(ZSmdiManagerDialog.this, "Select a device first", "Error", JOptionPane.ERROR_MESSAGE);
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
                        if (l.get(res) instanceof ZExternalDevice)
                            try {
                                SMDIAgent.setSmdiTargetCoupling(st.getHA_Id(), st.getSCSI_Id(), ((ZExternalDevice) l.get(res)).getDeviceIdentityMessage());
                            } catch (TargetNotSMDIException e1) {
                                e1.printStackTrace();
                            } catch (SmdiUnavailableException e1) {
                                e1.printStackTrace();
                            }
                } else
                    JOptionPane.showMessageDialog(ZSmdiManagerDialog.this, "No devices available", "Problem", JOptionPane.ERROR_MESSAGE);
            }
        });
        cButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        cButt.setToolTipText("Couple the selected SMDI Device to a Midi Device");

        /*  JButton dcButt = new JButton(new AbstractAction("Decouple from Midi Device") {
              public void actionPerformed(ActionEvent e) {
                  SMDIAgent.setSmdiTargetCoupling();
              }
          });
          dcButt.setAlignmentX(Component.LEFT_ALIGNMENT);
          dcButt.setToolTipText("Decouple the selected SMDI Device from a Midi Device");
          */

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
        ccButt.setToolTipText("Clear all the SMDI to Midi Device couplings");


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
                    new ZDefaultThread("Refresh SMDI") {
                        public void run() {
                            try {
                                SMDIAgent.refresh();
                            } finally {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        ((Component) e.getSource()).setEnabled(true);
                                    }
                                });
                            }
                        }
                    }.start();
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
        SMDIAgent.addSmdiListener(this);
    }

    public void dispose() {
        super.dispose();
        SMDIAgent.removeSmdiListener(this);
    }

    private void adjustSize() {
        //scrollPane.getViewport().setPreferredSize(scrollPane.getViewport().getView().getPreferredSize());
        // scrollPane.setViewport(scrollPane.getViewport());

        pack();
        Dimension d = getSize();
        int maxHeight = Preferences.userNodeForPackage(this.getClass()).getInt(PREF_smdiManagerMaxHeight, 400);

        if (d.getHeight() > maxHeight)
            setSize(new Dimension((int) d.getWidth(), maxHeight));
    }

    public void SmdiChanged() {
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
