package com.pcmsolutions.gui.midi;

import com.pcmsolutions.comms.ZMidiSystem;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
public class ZMidiManagerDialog extends ZDialog implements ZMidiSystem.MidiSystemListener, ComponentListener, ChangeListener {
    protected RowHeaderedAndSectionedTablePanel midiPanel;
    protected MidiManagerTable midiManagerTable;
    protected JScrollPane scrollPane;

    private static final String PREF_midiManagerMaxHeight = "midiManagerMaxHeight";
    private JCheckBox relCheck;

    public ZMidiManagerDialog(Frame ownerFrame, boolean modal) throws HeadlessException {
        super(ownerFrame, "Midi Manager", modal);
        JButton hideButt = new JButton(new AbstractAction("Hide") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        hideButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        hideButt.setToolTipText("Hide Midi Manager");

        getRootPane().setDefaultButton(hideButt);

        final JButton huntButt = new JButton();
        huntButt.setAction(new AbstractAction("Hunt") {
            public void actionPerformed(ActionEvent e) {
                huntButt.setEnabled(false);
                try {
                    Zoeos.getInstance().getSystemQ().getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            try {
                                Zoeos.getInstance().getDeviceManager().performHunt();
                            } finally {
                            }
                        }
                    }, "midiManagerHunt").post(new Callback() {
                        public void result(Exception e, boolean wasCancelled) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    huntButt.setEnabled(true);
                                }
                            });
                        }
                    });
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                    huntButt.setEnabled(true);
                }
            }
        });

        huntButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        huntButt.setToolTipText("Hunt For Devices");

        final JButton togglePermitButt = new JButton();
        togglePermitButt.setAction(new AbstractAction("Toggle permission") {
            public void actionPerformed(ActionEvent e) {
                int[] selRows = midiManagerTable.getSelectedRows();
                for (int i = 0, j = selRows.length; i < j; i++) {
                    MidiDevice.Info dev = (MidiDevice.Info) midiManagerTable.getModel().getValueAt(selRows[i], 0);
                    if (dev != null)
                        try {
                            ZMidiSystem.getInstance().togglePortPermitted(dev);
                        } catch (MidiUnavailableException e1) {
                            e1.printStackTrace();
                        }
                }
            }
        });
        togglePermitButt.setToolTipText("Toggle permission on selected ports");

        final JButton permitAllButt = new JButton();
        permitAllButt.setAction(new AbstractAction("Permit All") {
            public void actionPerformed(ActionEvent e) {
                ZMidiSystem.getInstance().permitAll();
            }
        });
        permitAllButt.setToolTipText("Permit all ports");

        /*
        final JButton toggleNeverCloseButt = new JButton();
        toggleNeverCloseButt.setAction(new AbstractAction("Toggle never closes") {
            public void actionPerformed(ActionEvent e) {
                int[] selRows = midiManagerTable.getSelectedRows();
                for (int i = 0, j = selRows.length; i < j; i++) {
                    MidiDevice.Info info = (MidiDevice.Info)midiManagerTable.getModel().getValueAt(selRows[i], 0);
                    if (info != null)
                        try {
                            ZMidiSystem.getInstance().togglePortNeverToBeClosed(info);
                        } catch (MidiUnavailableException e1) {
                            e1.printStackTrace();
                        }
                }
            }
        });
        toggleNeverCloseButt.setToolTipText("Toggle never closes");
        */

        relCheck = new JCheckBox(new AbstractAction("Release Midi resources when application is minimized (= Stop devices)") {
            public void actionPerformed(ActionEvent e) {
                ZoeosPreferences.ZPREF_releaseMidiOnMinimize.putValue(relCheck.isSelected());
            }
        });

        relCheck.setSelected(ZoeosPreferences.ZPREF_releaseMidiOnMinimize.getValue());
        ZoeosPreferences.ZPREF_releaseMidiOnMinimize.addChangeListener(this);
        JPanel bottomPanel = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }

        };
        bottomPanel.setLayout(new BorderLayout());

        JPanel bp1 = new JPanel();
        bp1.add(relCheck);
        bp1.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel bp2 = new JPanel();
        bp2.setLayout(new FlowLayout(FlowLayout.LEFT));
        bp2.add(hideButt);
        bp2.add(huntButt);
        bp2.add(togglePermitButt);
        bp2.add(permitAllButt);
        //bp2.add(toggleNeverCloseButt);

        bottomPanel.add(bp1, BorderLayout.NORTH);
        bottomPanel.add(bp2, BorderLayout.CENTER);

        AbstractAction ract = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                if (e.getSource() instanceof Component) {
                    ((Component) e.getSource()).setEnabled(false);
                    try {
                        Zoeos.getInstance().getSystemQ().getPostableTicket(new TicketRunnable() {
                            public void run() throws Exception {
                                try {
                                    //ZMidiSystem.getInstance().refresh(true);
                                } finally {
                                }
                            }
                        }, "performHunt").post(new Callback() {
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
        ract.putValue("tip", "Refresh Midi System");

        midiManagerTable = new MidiManagerTable();
        midiPanel = new RowHeaderedAndSectionedTablePanel().init(midiManagerTable, null, null, ract);
        scrollPane = new JScrollPane(midiPanel);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(UIColors.getTableRowHeight());
        scrollPane.getVerticalScrollBar().setBlockIncrement(UIColors.getTableRowHeight() * 4);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        midiManagerTable.addComponentListener(this);
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        adjustSize();
    }

    public void dispose() {
        super.dispose();
        ZMidiSystem.getInstance().removeMidiSystemListener(this);
        ZoeosPreferences.ZPREF_releaseMidiOnMinimize.removeChangeListener(this);
    }

    public void midiSystemChanged(ZMidiSystem msf) {

    }

    private void adjustSize() {
        //scrollPane.getViewport().setPreferredSize(scrollPane.getViewport().getView().getPreferredSize());
        //scrollPane.setViewport(scrollPane.getViewport());

        pack();
        Dimension d = getSize();

        int maxHeight = Preferences.userNodeForPackage(this.getClass()).getInt(PREF_midiManagerMaxHeight, 400);

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

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == ZoeosPreferences.ZPREF_releaseMidiOnMinimize)
            relCheck.setSelected(ZoeosPreferences.ZPREF_releaseMidiOnMinimize.getValue());
    }
}
