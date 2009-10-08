package com.pcmsolutions.gui.midi;

import com.pcmsolutions.comms.MidiSystemFacade;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.ZEditIgnoreTokensDialog;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;
import com.pcmsolutions.system.threads.ZDefaultThread;

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
public class ZMidiManagerDialog extends ZDialog implements MidiSystemFacade.MidiSystemListener, ComponentListener {
    protected RowHeaderedAndSectionedTablePanel midiPanel;
    protected MidiManagerTable midiManagerTable;
    protected JScrollPane scrollPane;
    protected ZEditIgnoreTokensDialog editIgnoreTokensDlg;

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
                Thread t = new ZDefaultThread("MidiManager Hunt") {
                    public void run() {
                        try {
                            Zoeos.getInstance().getDeviceManager().performHunt();
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    huntButt.setEnabled(true);
                                }
                            });
                        }
                    }
                };
                t.start();
            }
        });

        huntButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        huntButt.setToolTipText("Hunt For Devices");

        /*final JButton permitButt = new JButton();
        permitButt.setAction(new AbstractAction("Modify Port Ignore Tokens") {
            public void actionPerformed(ActionEvent e) {
                if ( editIgnoreTokensDlg == null)
                    editIgnoreTokensDlg = new ZEditIgnoreTokensDialog(ZoeosFrame.getInstance(), false);

                editIgnoreTokensDlg.show();
            }
        });
        permitButt.setToolTipText("Specify which ports ZoeOS is permitted to use");
         */

        final JButton permitButt = new JButton();
        permitButt.setAction(new AbstractAction("Permit") {
            public void actionPerformed(ActionEvent e) {
                int[] selRows = midiManagerTable.getSelectedRows();
                for (int i = 0,j = selRows.length; i < j; i++) {
                    Object tok = midiManagerTable.getModel().getValueAt(selRows[i], 0);
                    if (tok != null && !tok.equals(""))
                        MidiSystemFacade.getInstance().removeIgnoreToken(tok);
                }
            }
        });
        permitButt.setToolTipText("Permit selected ports");

        final JButton ignoreButt = new JButton();
        ignoreButt.setAction(new AbstractAction("Ignore") {
            public void actionPerformed(ActionEvent e) {
                int[] selRows = midiManagerTable.getSelectedRows();
                for (int i = 0,j = selRows.length; i < j; i++) {
                    Object tok = midiManagerTable.getModel().getValueAt(selRows[i], 0);
                    if (tok != null && !tok.equals(""))
                        MidiSystemFacade.getInstance().addIgnoreToken(tok);
                }
            }
        });
        ignoreButt.setToolTipText("Ignore selected ports");


        final JButton permitAllButt = new JButton();
        permitAllButt.setAction(new AbstractAction("Permit All") {
            public void actionPerformed(ActionEvent e) {
                MidiSystemFacade.getInstance().clearIgnoreTokens();
            }
        });
        permitAllButt.setToolTipText("Permit all ports");


        relCheck = new JCheckBox(new AbstractAction("Release Midi when Application Minimized (Stop Devices)") {
            public void actionPerformed(ActionEvent e) {
                ZoeosPreferences.ZPREF_releaseMidiOnMinimize.putValue(relCheck.isSelected());
            }
        });

        relCheck.setSelected(ZoeosPreferences.ZPREF_releaseMidiOnMinimize.getValue());

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
        bp2.add(permitButt);
        bp2.add(ignoreButt);
        bp2.add(permitAllButt);

        bottomPanel.add(bp1, BorderLayout.NORTH);
        bottomPanel.add(bp2, BorderLayout.CENTER);

        AbstractAction ract = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                if (e.getSource() instanceof Component) {
                    ((Component) e.getSource()).setEnabled(false);
                    new ZDefaultThread("Refresh Midi") {
                        public void run() {
                            try {
                                //MidiSystemFacade.getInstance().refresh(true);
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
        ract.putValue("tip", "Refresh Midi System");


        midiManagerTable = new MidiManagerTable();
        midiPanel = new RowHeaderedAndSectionedTablePanel().init(midiManagerTable, null, null, ract);
        scrollPane = new JScrollPane(midiPanel);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

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
        MidiSystemFacade.getInstance().removeMidiSystemListener(this);
    }

    public void midiSystemChanged(MidiSystemFacade msf) {

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
}
