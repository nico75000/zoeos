package com.pcmsolutions.device.EMU.E4.gui.device;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.event.CollapsiblePaneEvent;
import com.jidesoft.pane.event.CollapsiblePaneListener;
import com.jidesoft.swing.JideButton;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterSelectorDialog;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceOverviewTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.gui.desktop.DesktopBranch;
import com.pcmsolutions.gui.desktop.SessionableComponent;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * User: paulmeehan
 * Date: 09-Feb-2004
 * Time: 19:58:16
 */
public class DevicePanel extends JPanel implements TitleProvider, ZDisposable, SessionableComponent {
    private DeviceContext device;

    private JComponent commandComponent;
    private JComponent configurationComponent;
    private JComponent managementComponent;
    private JComponent snapshotComponent;

    private CollapsiblePane cmdPane;
    private CollapsiblePane configPane;
    private CollapsiblePane managePane;
    private CollapsiblePane snapshotPane;

    public void zDispose() {
        removeAll();
        device = null;
        commandComponent = null;
        configurationComponent = null;
        managementComponent = null;
        if (snapshotComponent instanceof CollapsiblePane && ((CollapsiblePane) snapshotComponent).getContentPane() instanceof ZDisposable)
            ((ZDisposable) ((CollapsiblePane) snapshotComponent).getContentPane()).zDispose();
        snapshotComponent = null;
    }

    public DevicePanel(DeviceContext device) throws Exception {
        this.device = device;

        commandComponent = makeCommandComponent();
        managementComponent = makeManagementComponent();
        snapshotComponent = makeSnapshotComponent();
        configurationComponent = makeConfigurationComponent();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(commandComponent);
        add(snapshotComponent);
        add(managementComponent);
        add(configurationComponent);
    }

    private JComponent makeCommandComponent() throws Exception {
        ZCommand[] cmds = device.getZCommands(device.getZCommandMarkers()[0]);
        for (ZCommand c : cmds)
            c.setTargets(device);
        JPanel p = new JPanel(new GridLayout(cmds.length, 1, 0, 0));
        for (int i = 0; i < cmds.length; i++)
            p.add(createHyperlinkButton(cmds[i]));
        return cmdPane = makeCollapsiblePane("Commands", p);
    }

    private static Font buttonFont = new Font("Arial", Font.ITALIC, 11);

    private static Font smallButtonFont = new Font("Arial", Font.ITALIC, 9);

    private static JComponent createHyperlinkButton(ZCommand zc) {
        return createHyperlinkButton(new ZCommandAction(zc, false));
    }

    private static JComponent createHyperlinkButton(Action a) {
        return createHyperlinkButton(a, false);
    }

    private static JComponent createHyperlinkButton(Action a, boolean smallFont) {
        final JideButton button = new JideButton(a) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        button.setAction(a);
        button.setButtonStyle(JideButton.HYPERLINK_STYLE);

        button.setOpaque(true);
        button.setPreferredSize(new Dimension(0, 20));
        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setRequestFocusEnabled(true);
        button.setFocusable(true);
        button.setFont((smallFont ? smallButtonFont : buttonFont));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JComponent makeConfigurationComponent() throws Exception {
        final JTable t = new JTable(device.getDeviceConfigTableModel()) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        GeneralTableCellRenderer gr = new GeneralTableCellRenderer() {
            {
                setForeground(UIColors.getTableFirstSectionFG());
                setBackground(UIColors.getTableFirstSectionHeaderBG());
            }
        };
        t.setDefaultRenderer(String.class.getClass(), gr);
        JPanel p = new JPanel(new BorderLayout()) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        p.add(t, BorderLayout.CENTER);
        configPane = makeCollapsiblePane("Configuration", p);
        configPane.addCollapsiblePaneListener(new CollapsiblePaneListener() {
            public void paneExpanding(CollapsiblePaneEvent event) {
                try {
                    device.refreshDeviceConfiguration(false).post();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void paneExpanded(CollapsiblePaneEvent event) {
            }

            public void paneCollapsing(CollapsiblePaneEvent event) {
            }

            public void paneCollapsed(CollapsiblePaneEvent event) {
            }
        });
        return configPane;
    }

    private JComponent makeManagementComponent() throws Exception {
        ArrayList actions = new ArrayList();
        AbstractAction a;
        a = new AbstractAction("Clear workspace") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.getViewManager().clearDeviceWorkspace().post();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Clear the entire device workspace");
        actions.add(a);
        a = new AbstractAction("Clear user presets") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.getViewManager().closeUserPresets().post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Clear any user preset editors from the device workspace");
        actions.add(a);
        a = new AbstractAction("Clear flash presets") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.getViewManager().closeFlashPresets().post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Clear any flash preset editors from the device workspace");
        actions.add(a);

        a = new AbstractAction("Clear empty presets") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.getViewManager().closeEmptyPresets().post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Clear any empty preset editors from the device workspace");
        actions.add(a);

        a = new AbstractAction("Clear voices") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.getViewManager().closeEmptyVoices().post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Clear any open voice editors from the device workspace");
        actions.add(a);

        a = new AbstractAction("Broadcast clear empty editors") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.getViewManager().brodcastCloseIfEmpty().post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Clear any empty editors from the device workspace");
        actions.add(a);

        a = new AbstractAction("Re-initialize preset flash") {
            public void actionPerformed(ActionEvent e) {
                if (UserMessaging.askYesNo("Are you sure you wish to re-initialize preset flash - any locally cached preset data will be lost?")) {
                    final boolean clear = UserMessaging.askYesNo("Clear any open flash presets from the device workspace (recommended)?");
                    if (clear) {
                        try {
                            device.getViewManager().closeFlashPresets().send(0);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        // put the actual command on EDT to sync with clear of desktop
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    device.reinitializePresetFlash().post();
                                } catch (ResourceUnavailableException e1) {
                                    UserMessaging.flashWarning(DevicePanel.this, e1.getMessage());
                                }
                            }
                        });
                    } else
                        try {
                            device.reinitializePresetFlash().post();
                        } catch (ResourceUnavailableException e1) {
                            UserMessaging.flashWarning(DevicePanel.this, e1.getMessage());
                        }
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Re-initialize preset flash");
        actions.add(a);

        a = new AbstractAction("Save device state (session)") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.saveDeviceState().post();
                } catch (Exception e1) {
                    UserMessaging.flashWarning(DevicePanel.this, e1.getMessage());
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Dump device state (session) to disk now (this is usually done automatically by the system when ZoeOS is shut down or during a device remove)");
        actions.add(a);

        a = new AbstractAction("All sounds off") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.sampleMemoryDefrag(true).post();
                } catch (Exception e1) {
                    UserMessaging.flashWarning(DevicePanel.this, e1.getMessage());
                }
            }
        };
        //a.putValue(Action.SHORT_DESCRIPTION, "Send 'AllNotesOff' midi command to all reachable midi channels of the device");
        a.putValue(Action.SHORT_DESCRIPTION, "All sounds off (via sample memory defrag )");
        actions.add(a);

        a = new AbstractAction("Cancel pending auditions") {
            public void actionPerformed(ActionEvent e) {
                try {
                    device.cancelAuditions().post();
                } catch (Exception e1) {
                    UserMessaging.flashWarning(DevicePanel.this, e1.getMessage());
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Cancel any queued auditions");
        actions.add(a);

        a = new AbstractAction("Configure preset user table") {
            public void actionPerformed(ActionEvent e) {
                ParameterSelectorDialog dlg = null;
                try {
                    java.util.List pdl = device.getDeviceParameterContext().getVoiceContext().getAllParameterDescriptors();
                    GeneralParameterDescriptor[] pds = (GeneralParameterDescriptor[]) pdl.toArray(new GeneralParameterDescriptor[pdl.size()]);
                    dlg = new ParameterSelectorDialog(ZoeosFrame.getInstance(), "Configure preset user table", pds, ParameterUtilities.userTableGroupedParameters, ParameterUtilities.userTableIgnoreIds, VoiceOverviewTableModel.getUserIdList());
                    dlg.setVisible(true);
                    Integer[] ids = dlg.getSelectedIds();
                    if (dlg.isApplied() && ids != null)
                        VoiceOverviewTableModel.setUserIdList(ids);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };
        a.putValue(Action.SHORT_DESCRIPTION, "Change the parameters displayed on the preset user table.");
        actions.add(a);

        JPanel p = new JPanel(new GridLayout(actions.size(), 1, 0, 0));
        for (int i = 0, j = actions.size(); i < j; i++)
            p.add(createHyperlinkButton((Action) actions.get(i)));
        return managePane = makeCollapsiblePane("Management Tasks", p);
    }

    private static class SnapshotPanel extends JPanel implements ZDisposable {
        private DeviceContext device;

        private com.pcmsolutions.device.EMU.E4.desktop.ViewManager.Listener viewManListener;

        public SnapshotPanel(DeviceContext device) {
            super(new BorderLayout());
            this.device = device;
            viewManListener = new com.pcmsolutions.device.EMU.E4.desktop.ViewManager.Listener() {
                public void viewManagerStateChanged(DeviceContext device) {
                    if (device == SnapshotPanel.this.device)
                        update(device.getViewManager().getSnapshots());
                }
            };
            device.getViewManager().addViewManagerListener(viewManListener);
            update(device.getViewManager().getSnapshots());
        }

        private void update(final DesktopBranch[] snaps) {
            JPanel gp = new JPanel(new GridLayout(snaps.length, 1, 0, 0));
            for (int i = 0; i < snaps.length; i++) {
                final int f_i = i;
                JPanel bg = new ZJPanel(new GridLayout(1, 2, 0, 0)) {
                    public Color getBackground() {
                        return UIColors.getDefaultBG();
                    }

                    public Color getForeground() {
                        return UIColors.getDefaultFG();
                    }
                };
                JLabel title = new JLabel(snaps[i].getTitle());
                title.setFont(buttonFont);
                bg.add(title);
                Box bb = new Box(BoxLayout.X_AXIS);
                bb.add(createHyperlinkButton(new AbstractAction(" [ Activate") {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            device.getViewManager().modifyBranch(snaps[f_i], true, 1).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                    }
                }, true));
                bb.add(createHyperlinkButton(new AbstractAction("Merge") {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            device.getViewManager().openDesktopElements((snaps[f_i].getDesktopElements())).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                    }
                }, true));
                bb.add(createHyperlinkButton(new AbstractAction("Delete ]") {
                    public void actionPerformed(ActionEvent e) {
                        device.getViewManager().removeSnapshot(snaps[f_i]);
                    }
                }, true));
                bg.add(bb);
                gp.add(bg);
            }
            removeAll();
            add(gp);
            gp.revalidate();
            gp.repaint();
        }

        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }

        public void zDispose() {
            if (viewManListener != null)
                device.getViewManager().removeViewManagerListener(viewManListener);
        }
    }

    private JComponent makeSnapshotComponent() throws Exception {
        SnapshotPanel p = new SnapshotPanel(device);
        return makeCollapsiblePane("Workspace snapshots", p);
    }

    private CollapsiblePane makeCollapsiblePane(String title, JComponent content) {
        snapshotPane = new CollapsiblePane(title) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        snapshotPane.setContentPane(content);
        snapshotPane.setBorder(new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true));
        return snapshotPane;
    }

    public String getTitle() {
        return device.getTitle();
    }

    public String getReducedTitle() {
        return device.getReducedTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        device.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        device.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return device.getIcon();
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    private static final String cmdPaneTagId = DevicePanel.class.toString() + "cmdCollapsed";
    private static final String configPaneTagId = DevicePanel.class.toString() + "configCollapsed";
    private static final String managePaneTagId = DevicePanel.class.toString() + "manageCollapsed";
    private static final String snapshotPaneTagId = DevicePanel.class.toString() + "snapshotCollapsed";

    public String retrieveComponentSession() {
        StringBuffer sb = new StringBuffer();
        sb.append(ZUtilities.makeTaggedField(cmdPaneTagId, String.valueOf(cmdPane.isCollapsed())));
        sb.append(ZUtilities.makeTaggedField(configPaneTagId, String.valueOf(configPane.isCollapsed())));
        sb.append(ZUtilities.makeTaggedField(managePaneTagId, String.valueOf(managePane.isCollapsed())));
        sb.append(ZUtilities.makeTaggedField(snapshotPaneTagId, String.valueOf(snapshotPane.isCollapsed())));
        return sb.toString();
    }

    public void restoreComponentSession(String sessStr) {
        if (sessStr != null && !sessStr.equals("")) {
            String field;
            try {
                field = ZUtilities.extractTaggedField(sessStr, cmdPaneTagId);
                if (field != null)
                    cmdPane.setCollapsed(Boolean.parseBoolean(field));

                field = ZUtilities.extractTaggedField(sessStr, configPaneTagId);
                if (field != null)
                    configPane.setCollapsed(Boolean.parseBoolean(field));

                field = ZUtilities.extractTaggedField(sessStr, managePaneTagId);
                if (field != null)
                    managePane.setCollapsed(Boolean.parseBoolean(field));

                field = ZUtilities.extractTaggedField(sessStr, snapshotPaneTagId);
                if (field != null)
                    snapshotPane.setCollapsed(Boolean.parseBoolean(field));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
