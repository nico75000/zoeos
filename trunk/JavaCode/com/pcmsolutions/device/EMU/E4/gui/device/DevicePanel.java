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
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.gui.ZCommandAction;
import com.pcmsolutions.gui.desktop.DesktopBranch;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.threads.ZDefaultThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * User: paulmeehan
 * Date: 09-Feb-2004
 * Time: 19:58:16
 */
public class DevicePanel extends JPanel implements TitleProvider, ZDisposable {
    private DeviceContext device;

    private JComponent commandComponent;
    private JComponent configurationComponent;
    private JComponent managementComponent;
    private JComponent snapshotComponent;

    public void zDispose() {
        removeAll();
        device = null;
        commandComponent = null;
        configurationComponent = null;
        managementComponent = null;
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
        ZCommand[] cmds = device.getZCommands();
        JPanel p = new JPanel(new GridLayout(cmds.length + 1, 1, 0, 0));
        for (int i = 0; i < cmds.length; i++)
            p.add(createHyperlinkButton(cmds[i]));
        p.add(createHyperlinkButton(new AbstractAction("Clear workspace") {
            public void actionPerformed(ActionEvent e) {
                new ZDefaultThread() {
                    public void run() {
                        device.getViewManager().clearDeviceWorkspace().start();
                    }
                }.start();
            }
        }));
        return makeCollapsiblePane("Commands", p);
    }

    private static Font buttonFont = new Font("Arial", Font.ITALIC, 12);
    private static Font smallButtonFont = new Font("Arial", Font.ITALIC, 10);

    private static JComponent createHyperlinkButton(ZCommand zc) {
        return createHyperlinkButton(new ZCommandAction(new ZCommand[]{zc}));
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
            protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
                super.setupLook(table, value, isSelected, row, column);
                setForeground(UIColors.getTableFirstSectionFG());
                setBackground(UIColors.getTableFirstSectionBG());
            }
        };
        t.setDefaultRenderer(String.class, gr);
        JPanel p = new JPanel(new BorderLayout()) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        p.add(t, BorderLayout.CENTER);
        CollapsiblePane cp = makeCollapsiblePane("Configuration", p);
        cp.addCollapsiblePaneListener(new CollapsiblePaneListener() {
            public void paneExpanding(CollapsiblePaneEvent event) {
                device.refreshDeviceConfiguration(false);
            }

            public void paneExpanded(CollapsiblePaneEvent event) {
            }

            public void paneCollapsing(CollapsiblePaneEvent event) {
            }

            public void paneCollapsed(CollapsiblePaneEvent event) {
            }
        });
        return cp;
    }

    private JComponent makeManagementComponent() throws Exception {
        JPanel p = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        return makeCollapsiblePane("Management Tasks", p);
    }

    private static class SnapshotPanel extends JPanel {
        private DeviceContext device;

        public SnapshotPanel(DeviceContext device) {
            super(new BorderLayout());
            this.device = device;
            device.getViewManager().addViewManagerListener(new com.pcmsolutions.device.EMU.E4.desktop.ViewManager.Listener() {
                public void viewManagerStateChanged(DeviceContext device) {
                    if (device == SnapshotPanel.this.device)
                        update(device.getViewManager().getSnapshots());
                }
            });
            update(device.getViewManager().getSnapshots());
        }

        private void update(final DesktopBranch[] snaps) {
            JPanel gp = new JPanel(new GridLayout(snaps.length, 1, 0, 0));
            for (int i = 0; i < snaps.length; i++) {
                final int f_i = i;
                JPanel bg = new JPanel(new GridLayout(1, 2, 0, 0)) {
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
                        //UserMessaging.showInfo("Snap!");
                        device.getViewManager().modifyBranch(snaps[f_i], true, 1).start();
                        //snaps[i]
                    }
                }, true));
                bb.add(createHyperlinkButton(new AbstractAction("Merge") {
                    public void actionPerformed(ActionEvent e) {
                        //UserMessaging.showInfo("Snap!");
                        new ZDefaultThread() {
                            public void run() {
                                device.getViewManager().openDesktopElements((snaps[f_i].getDesktopElements()));
                            }
                        }.start();
                        //snaps[i]
                    }
                }, true));
                bb.add(createHyperlinkButton(new AbstractAction("Delete ]") {
                    public void actionPerformed(ActionEvent e) {
                        //UserMessaging.showInfo("Snap!");
                        new ZDefaultThread() {
                            public void run() {
                                device.getViewManager().removeSnapshot(snaps[f_i]);
                            }
                        }.start();
                        //snaps[i]
                    }
                }, true));
                bg.add(bb);
                gp.add(bg);
            }
            removeAll();
            add(gp);
        }

        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }
    }

    private JComponent makeSnapshotComponent() throws Exception {
        SnapshotPanel p = new SnapshotPanel(device);
        return makeCollapsiblePane("Workspace snapshots", p);
    }

    private CollapsiblePane makeCollapsiblePane(String title, JComponent content) {
        CollapsiblePane cp = new CollapsiblePane(title) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        cp.setContentPane(content);
        cp.setBorder(new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true));
        return cp;
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
}
