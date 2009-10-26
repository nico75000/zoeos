/*
 * ZoeosFrame.java
 *
 * Created on 04 October 2002, 00:37
 */

package com.pcmsolutions.gui;

import com.incors.plaf.alloy.AlloyFontTheme;
import com.incors.plaf.alloy.AlloyLookAndFeel;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.grid.CellEditorManager;
import com.jidesoft.grid.CellRendererManager;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.status.MemoryStatusBarItem;
import com.jidesoft.status.StatusBar;
import com.jidesoft.swing.JideBoxLayout;
import com.pcmsolutions.comms.MidiSystemFacade;
import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.desktop.*;
import com.pcmsolutions.gui.license.ZLicenseManagerDialog;
import com.pcmsolutions.gui.midi.ZMidiManagerDialog;
import com.pcmsolutions.gui.smdi.ZSmdiManagerDialog;
import com.pcmsolutions.smdi.SMDIAgent;
import com.pcmsolutions.system.BrowserControl;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Enumeration;
import java.util.prefs.Preferences;

/**
 *
 * @author  pmeehan
 */

public class ZoeosFrame extends ZJFrame {
    private Zoeos zi;
    private static ZoeosFrame INSTANCE;
    private ZDeviceManagerDialog deviceManager;
    private ZMidiManagerDialog midiManager;
    private ZSmdiManagerDialog smdiManager;
    private ZLicenseManagerDialog licenseManager;
    private TipOfTheDayDialog tipOfTheDay;

    private static final String DOCK_PROFILE_KEY = "Zoeos_Layouts";

    private javax.swing.JMenu jmFile;
    private javax.swing.JMenu jmManage;
    private javax.swing.JMenu jmWindow;
    private javax.swing.JMenu jmHelp;

    public JMenuItem redoMenuItem;
    public JMenuItem undoMenuItem;
    private boolean autohideAll = false;

    private javax.swing.JMenuItem jmiDeviceManager;
    private javax.swing.JMenuItem jmiMidiManager;
    private javax.swing.JMenuItem jmiSMDIManager;
    private javax.swing.JMenuItem jmiTaskManager;

    private javax.swing.JMenuItem jmiLicenseManager;

    private javax.swing.JMenuItem jmiTipOfTheDay;
    private javax.swing.JMenuItem jmiProductTour;
    private javax.swing.JMenuItem jmiHelp;
    private javax.swing.JMenuItem jmiRequestFunctionality;
    private javax.swing.JMenuItem jmiReportBug;
    private javax.swing.JMenuItem jmiAboutBox;

    private javax.swing.JMenuItem jmiExit;
    private javax.swing.JMenuBar jMainMenu;

    private HelpBroker hb;

    private class ShutdownDialog extends ZDialog {
        public ShutdownDialog() throws HeadlessException {
            super(ZoeosFrame.this, "ZoeOS is shutting down", true);
            JProgressBar pb = new JProgressBar(0, 100);
            pb.setString(ZUtilities.makeExactLengthString("Shutdown", 80));
            pb.setStringPainted(true);
            pb.setIndeterminate(true);
            getContentPane().add(pb);
            pack();
        }
    };


    private Runnable shutdownZoeos = new Runnable() {
        public void run() {
            final JDialog sd = new ShutdownDialog();
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        sd.show();
                    }
                });
                //ZoeosFrame.this.getDockingManager().saveLayoutDataToFile("ZoeOSLayout");
                ZoeosFrame.this.getDockingManager().saveLayoutDataAs(ZDesktopManager.LAYOUT_LAST);
                Zoeos.getInstance().zDispose();
                MidiSystemFacade.getInstance().zDispose();
            } finally {
                sd.dispose();
                ZoeosFrame.this.dispose();
                System.exit(0);
            }
        }
    };

    public static ZoeosFrame getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ZoeosFrame();
            INSTANCE.init();
        }
        return INSTANCE;
    }

    /** Creates new form ZoeosFrame */
    //  private static final String PREF_uses = "zoeos" + Zoeos.versionStr + "uses ";

    public ZoeosFrame() {
        URL url = ZoeosFrame.class.getResource("/zoeosFrameIcon.gif");

        if (url != null)
            this.setIconImage(new ImageIcon(url).getImage());
        this.setTitle("ZoeOS");
        zi = Zoeos.getInstance();
        zi.setZoeosFrame(this);
        //Dimension scrsize = Toolkit.getDefaultToolkit().getScreenSize();
        //setSize(scrsize.width, (scrsize.height - 25));
        //setLocation(0, 0);
        //this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        initJIDE();
        initComponents();
    }

    private void init() {
        try {
            deviceManager = new ZDeviceManagerDialog(this, false);
            midiManager = new ZMidiManagerDialog(this, false);
            assertSmdiManager();
            licenseManager = new ZLicenseManagerDialog(this, false);
            tipOfTheDay = new TipOfTheDayDialog();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not initialize main ZoeOS window. Exiting.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    private void initJIDE() {
        this.getDockingManager().setUsePref(false);
        this.getDockingManager().setProfileKey(DOCK_PROFILE_KEY);
        this.getDockingManager().setUseFrameBounds(true);
        this.getDockingManager().setUseFrameState(true);

        getDockingManager().setUndoLimit(10);
        getDockingManager().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                refreshUndoRedoMenuItems();
            }
        });
        ObjectConverterManager.initDefaultConverter();
        CellEditorManager.initDefaultEditor();
        CellRendererManager.initDefaultRenderer();

        GeneralTableCellRenderer gr = new GeneralTableCellRenderer() {
            protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
                super.setupLook(table, value, isSelected, row, column);
                setForeground(UIColors.getTableFirstSectionFG());
                setBackground(UIColors.getTableFirstSectionBG());
            }
        };
        CellRendererManager.registerRenderer(Boolean.class, gr);
        CellRendererManager.registerRenderer(Integer.class, gr);
        CellRendererManager.registerRenderer(String.class, gr);
        CellRendererManager.registerRenderer(Double.class, gr);

        zDesktopManager = new Impl_ZDesktopManager(getDockingManager());

        this.getDockingManager().getWorkspace().setLayout(new BorderLayout());
        this.getDockingManager().getWorkspace().add(zDesktopManager.getWorkspaceViewTreeModel().getRootDocumentPane(), BorderLayout.CENTER);
        initFromZPrefs();
    }

    private void initFromZPrefs() {
        ChangeListener prefListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == ZoeosPreferences.ZPREF_sideBarRollover)
                    getDockingManager().setSidebarRollover(ZoeosPreferences.ZPREF_sideBarRollover.getValue());
                else if (e.getSource() == ZoeosPreferences.ZPREF_animationSteps)
                    getDockingManager().setSteps(ZoeosPreferences.ZPREF_animationSteps.getValue());
                else if (e.getSource() == ZoeosPreferences.ZPREF_animationStepDelay)
                    getDockingManager().setStepDelay(ZoeosPreferences.ZPREF_animationStepDelay.getValue());
                else if (e.getSource() == ZoeosPreferences.ZPREF_animationInitDelay)
                    getDockingManager().setInitDelay(ZoeosPreferences.ZPREF_animationInitDelay.getValue());
                else if (e.getSource() == ZoeosPreferences.ZPREF_autoHideShowContentsHidden)
                    getDockingManager().setAuthideShowingContentHidden(ZoeosPreferences.ZPREF_autoHideShowContentsHidden.getValue());
            }
        };
        ZoeosPreferences.ZPREF_sideBarRollover.addChangeListener(prefListener);
        ZoeosPreferences.ZPREF_animationSteps.addChangeListener(prefListener);
        ZoeosPreferences.ZPREF_animationStepDelay.addChangeListener(prefListener);
        ZoeosPreferences.ZPREF_animationInitDelay.addChangeListener(prefListener);
        ZoeosPreferences.ZPREF_autoHideShowContentsHidden.addChangeListener(prefListener);
        getDockingManager().setSidebarRollover(ZoeosPreferences.ZPREF_sideBarRollover.getValue());
        getDockingManager().setSteps(ZoeosPreferences.ZPREF_animationSteps.getValue());
        getDockingManager().setStepDelay(ZoeosPreferences.ZPREF_animationStepDelay.getValue());
        getDockingManager().setInitDelay(ZoeosPreferences.ZPREF_animationInitDelay.getValue());
        getDockingManager().setAuthideShowingContentHidden(ZoeosPreferences.ZPREF_autoHideShowContentsHidden.getValue());
    }

    private void assertSmdiManager() {
        if (SMDIAgent.isSmdiAvailable())
            smdiManager = new ZSmdiManagerDialog(this, false);
        else
            smdiManager = null;
    }

    public void showDeviceManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                deviceManager.show();
            }
        });
    }

    public void hideDeviceManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                deviceManager.hide();
            }
        });
    }

    public void showTips() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tipOfTheDay.show();
            }
        });
    }

    public void hideTips() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tipOfTheDay.hide();
            }
        });
    }

    public void showMidiManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                midiManager.show();
            }
        });
    }

    public void hideMidiManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                midiManager.hide();
            }
        });
    }

    public void showLicenseManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                licenseManager.show();
            }
        });
    }

    public void hideLicenseManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                licenseManager.hide();
            }
        });
    }

    public void showSmdiManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                assertSmdiManager();
                if (smdiManager == null)
                    JOptionPane.showMessageDialog(ZoeosFrame.this, "SMDI not installed");
                else
                    smdiManager.show();
            }
        });
    }

    public void hideSmdiManager() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (smdiManager != null)
                    smdiManager.hide();
            }
        });
    }

    public void shutdown() {
        FlashMsg.globalDisable = true;
        this.pmb.setShowable(false);
        new Thread(shutdownZoeos).start();
    }

    private void initComponents() {

        createMenuBar();
        createStatusBar();

        final ViewInstance vi = SystemViewFactory.providePropertiesView();
        try {
            zDesktopManager.addDesktopElement(new AbstractDesktopElement(vi.getViewPath(), true, StaticActivityContext.FALSE, new Impl_DesktopNodeDescriptor()) {
                protected JComponent createView() throws ComponentGenerationException {
                    return vi.getView();
                }

                public boolean hasExpired() {
                    return false;
                }

                public int compareTo(Object o) {
                    return -1;
                }

                public DesktopElement getCopy() {
                    return this;
                }
            });
        } catch (ComponentGenerationException e) {
            e.printStackTrace();
        } catch (ChildViewNotAllowedException e) {
            e.printStackTrace();
        } catch (LogicalHierarchyException e) {
            e.printStackTrace();
        }
    }

    private void createMenuBar() {
        jMainMenu = new JMenuBar();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                shutdown();
            }
        });
        createFileMenu();
        createManageMenu();
        createWindowMenu();
        createHelpMenu();
        finalizeMenus();
    }

    private void createStatusBar() {
        StatusBar statusBar = new StatusBar();

        /*
        final LabelStatusBarItem label = new LabelStatusBarItem("Line");
        label.setText("100:42");
        label.setPreferredWidth(60);
        label.setAlignment(JLabel.CENTER);
        statusBar.addDesktopElement(label, JideBoxLayout.FLEXIBLE);
        */

        /*
        final OvrInsStatusBarItem ovr = new OvrInsStatusBarItem();
         ovr.setPreferredWidth(100);
         ovr.setAlignment(JLabel.CENTER);
         statusBar.addDesktopElement(ovr, JideBoxLayout.FLEXIBLE);
        */

        //final TimeStatusBarItem time = new TimeStatusBarItem();
        //statusBar.addDesktopElement(time, JideBoxLayout.FLEXIBLE);

        final MemoryStatusBarItem gc = new MemoryStatusBarItem();
        gc.setPreferredWidth(100);
        statusBar.add(gc, JideBoxLayout.FIX);

        getContentPane().add(statusBar, BorderLayout.AFTER_LAST_LINE);
    }

    private void finalizeMenus() {
        jMainMenu.add(jmFile);
        jMainMenu.add(jmManage);
        jMainMenu.add(jmWindow);
        jMainMenu.add(jmHelp);
        setJMenuBar(jMainMenu);
    }

    private void createWindowMenu() {

        jmWindow = new JMenu("Window");
        jmWindow.setMnemonic('W');

        JMenuItem item;
        JMenu submenu;

        undoMenuItem = new JMenuItem("Undo");
        jmWindow.add(undoMenuItem);
        redoMenuItem = new JMenuItem("Redo");
        jmWindow.add(redoMenuItem);
        undoMenuItem.setEnabled(false);
        redoMenuItem.setEnabled(false);

        undoMenuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getDockingManager().undo();
                refreshUndoRedoMenuItems();
            }
        });
        redoMenuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getDockingManager().redo();
                refreshUndoRedoMenuItems();
            }
        });

        jmWindow.addSeparator();

        submenu = new JMenu("Load");

        item = new JMenuItem("Custom Layout 1");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    getDockingManager().loadLayoutDataFrom(ZDesktopManager.LAYOUT_CUSTOM_1);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        submenu.add(item);

        item = new JMenuItem("Custom Layout 2");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    getDockingManager().loadLayoutDataFrom(ZDesktopManager.LAYOUT_CUSTOM_2);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        submenu.add(item);

        item = new JMenuItem("Custom Layout 3");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    getDockingManager().loadLayoutDataFrom(ZDesktopManager.LAYOUT_CUSTOM_3);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        submenu.add(item);

        jmWindow.add(submenu);

        jmWindow.addSeparator();

        submenu = new JMenu("Save");
        item = new JMenuItem("as Custom Layout 1");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getDockingManager().saveLayoutDataAs(ZDesktopManager.LAYOUT_CUSTOM_1);
            }
        });
        submenu.add(item);

        item = new JMenuItem("as Custom Layout 2");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getDockingManager().saveLayoutDataAs(ZDesktopManager.LAYOUT_CUSTOM_2);
            }
        });
        submenu.add(item);

        item = new JMenuItem("as Custom Layout 3");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getDockingManager().saveLayoutDataAs(ZDesktopManager.LAYOUT_CUSTOM_3);
            }
        });
        submenu.add(item);

        jmWindow.add(submenu);

        jmWindow.addSeparator();

        item = new JMenuItem("Reset Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getDockingManager().setUseFrameBounds(false);
                getDockingManager().setUseFrameState(false);
                getDockingManager().resetToDefault();
            }
        });
        jmWindow.add(item);

        jmWindow.addSeparator();

        item = new JMenuItem("Toggle Auto Hide Rollover");
        item.setMnemonic('T');
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //getDockingManager().setSidebarRollover(!getDockingManager().isSidebarRollover());
                ZoeosPreferences.ZPREF_sideBarRollover.toggleValue();
            }
        });
        jmWindow.add(item);
    }

    private void refreshUndoRedoMenuItems() {
        undoMenuItem.setEnabled(getDockingManager().getUndoManager().canUndo());
        undoMenuItem.setText(getDockingManager().getUndoManager().getUndoPresentationName());
        redoMenuItem.setEnabled(getDockingManager().getUndoManager().canRedo());
        redoMenuItem.setText(getDockingManager().getUndoManager().getRedoPresentationName());
    }

    private void createHelpMenu() {
        jmHelp = new JMenu();

        jmiLicenseManager = new JMenuItem();
        jmiHelp = new JMenuItem();
        jmiTipOfTheDay = new JMenuItem();
        jmiProductTour = new JMenuItem();
        jmiRequestFunctionality = new JMenuItem();
        jmiReportBug = new JMenuItem();
        jmiAboutBox = new JMenuItem();
        jmHelp.setText("Help");
        jmHelp.setMnemonic(KeyEvent.VK_H);
        jmHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            }
        });

        // Find the HelpSet File and create the HelpSet object:
        //String helpHS = "zoeosHelpSet.hs";

        String helpHS = "zoeosHelp.hs";
        ClassLoader cl = ZoeosFrame.class.getClassLoader();
        try {
            URL hsURL = HelpSet.findHelpSet(cl, helpHS);
            hb = new HelpSet(null, hsURL).createHelpBroker();
        } catch (Exception ee) {
            // Say what the exception really is
            System.out.println("HelpSet " + ee.getMessage());
            System.out.println("HelpSet " + helpHS + " not found");
        }

        jmiHelp.setText("Help Contents");
        jmiHelp.setMnemonic(KeyEvent.VK_H);

        if (hb != null) {
            jmiHelp.addActionListener(new CSH.DisplayHelpFromSource(hb));
        } else
            jmiHelp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    UserMessaging.showInfo("Help not available");
                }
            });

        jmHelp.add(jmiHelp);

        jmiProductTour.setText("Zuonics Homepage & Product Tour");
        jmiProductTour.setMnemonic(KeyEvent.VK_B);
        jmiProductTour.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                BrowserControl.displayURL("http://www.zuonics.com");
            }
        });
        jmHelp.add(jmiProductTour);
        jmHelp.addSeparator();

        jmiRequestFunctionality.setText("Enhancement request");
        jmiRequestFunctionality.setMnemonic(KeyEvent.VK_R);
        jmiRequestFunctionality.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                BrowserControl.displayURL("mailto:request@zuonics.com?subject=Enhancement request for " + Zoeos.versionStr);
            }
        });
        jmHelp.add(jmiRequestFunctionality);

        jmiReportBug.setText("Report a Bug");
        jmiReportBug.setMnemonic(KeyEvent.VK_B);
        jmiReportBug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                BrowserControl.displayURL("mailto:bugs@zuonics.com?subject=Bug in " + Zoeos.versionStr + "&body=< PASTE DEVICE CONFIGURATION HERE >");
                //mailto:astark1@unl.edu?subject=Comments from MailTo Syntax Page
                //JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Not available in demo");
            }
        });
        jmHelp.add(jmiReportBug);

        jmiLicenseManager.setText("Manage License Keys");
        jmiLicenseManager.setMnemonic(KeyEvent.VK_L);
        jmiLicenseManager.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                licenseManager.show();
                //JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Not available in demo");
            }
        });
//        jmHelp.add(jmiLicenseManager);

        jmHelp.addSeparator();

        jmiAboutBox.setText("About");
        jmiAboutBox.setMnemonic(KeyEvent.VK_A);
        jmiAboutBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), Zoeos.aboutMessage + "\n\nJVM version " +
                        System.getProperty("java.version") + "\n" +
                        " by " + System.getProperty("java.vendor"), "About ZoeOS", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(ZoeosFrame.class.getResource("/zoeosFrameIcon.gif"))
                );
            }
        });
        jmHelp.add(jmiAboutBox);
    }

    private void createManageMenu() {
        jmManage = new JMenu();

        jmiDeviceManager = new JMenuItem();
        jmiMidiManager = new JMenuItem();
        jmiSMDIManager = new JMenuItem();
        jmiTaskManager = new JMenuItem();

        jmManage.setText("Manage");
        jmManage.setMnemonic(KeyEvent.VK_M);

        jmiDeviceManager.setText("Devices");
        jmiDeviceManager.setMnemonic(KeyEvent.VK_D);
        jmiDeviceManager.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deviceManager.show();
            }
        });
        jmManage.add(jmiDeviceManager);

        jmiMidiManager.setText("Midi");
        jmiMidiManager.setMnemonic(KeyEvent.VK_M);
        jmiMidiManager.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                midiManager.show();
            }
        });
        jmManage.add(jmiMidiManager);

        jmiSMDIManager.setText("SMDI");
        jmiSMDIManager.setMnemonic(KeyEvent.VK_S);
        jmiSMDIManager.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                assertSmdiManager();
                if (smdiManager == null)
                    JOptionPane.showMessageDialog(ZoeosFrame.this, "SMDI not installed");
                else
                    smdiManager.show();
            }
        });
        jmManage.add(jmiSMDIManager);

        jmiTaskManager.setText("Show Tasks");
        jmiTaskManager.setMnemonic(KeyEvent.VK_T);
        jmiTaskManager.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showTasks();
            }
        });
        jmManage.add(jmiTaskManager);
    }

    private void createFileMenu() {
        jmFile = new JMenu();
        jmFile.setText("File");
        jmFile.setMnemonic(KeyEvent.VK_F);

        jmiExit = new JMenuItem();
        jmiExit.setText("Exit");
        jmiExit.setMnemonic(KeyEvent.VK_X);
        jmiExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(ZoeosFrame.this, "Are you sure you want to exit?", "Shutdown ZoeOS", JOptionPane.YES_NO_OPTION) == 0)
                    shutdown();
            }
        });
        jmFile.add(jmiExit);
    }

    private Impl_ZDesktopManager zDesktopManager;

    public ZDesktopManager getZDesktopManager() {
        return zDesktopManager;
    }

    public static void main(String args[]) {
        try {

            try {
                // initLook();
                com.jidesoft.utils.Lm.verifyLicense("Zuonics", "ZoeOS", "odQ6AtjgQBLkRNQQNqiFlMcc2lIq1Pe2");

                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
                /*
                com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.licenseCode", "a#PCM_Solutions_Ltd#1pj3nkw#a522i8");
                com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.isLookAndFeelFrameDecoration", "true");
                AlloyLookAndFeel.setProperty("alloy.isButtonPulseEffectEnabled", "false");


                //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.glass.GlassTheme(new CustomFontTheme(new String[]{"Arial", "Courier"}, 10, 9));
                //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.acid.AcidTheme(new CustomFontTheme(new String[]{"Arial", "Courier"}, 10, 9));
                //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.glass.GlassTheme();
                //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.bedouin.BedouinTheme(new CustomFontTheme(new String[]{"Arial", "Courier"}, 11, 11));
                //                com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.bedouin.BedouinTheme(new CustomFontTheme(new String[]{"Microsoft Sans Serif", "Arial"}, 11, 11));

                com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.bedouin.BedouinTheme(new AlloyFontTheme() {
                    //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.acid.AcidTheme(new AlloyFontTheme() {
                    public FontUIResource getControlTextFont() {
                        return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
                    }

                    public FontUIResource getSystemTextFont() {
                        return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
                    }

                    public FontUIResource getUserTextFont() {
                        return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
                    }

                    public FontUIResource getMenuTextFont() {
                        return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 11);
                    }

                    public FontUIResource getWindowTitleFont() {
                        return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
                    }

                    public FontUIResource getSubTextFont() {
                        return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
                    }
                });

                javax.swing.LookAndFeel alloyLnF = new com.incors.plaf.alloy.AlloyLookAndFeel(theme);
                //AlloyJideLookAndFeel ajlf = new AlloyJideLookAndFeel();
                //ajlf.setTheme(theme, true);
                javax.swing.UIManager.setLookAndFeel(LookAndFeelFactory.ALLOY_LNF);
                */

                LookAndFeelFactory.setDefaultLookAndFeel();
                LookAndFeelFactory.installJideExtension();
                Font defFont = new Font("Arial", Font.PLAIN, 11);
                // UIManager.getDefaults().put("Label.font", defFont);

                //using the put keys to set the font size for the different visual components.
                for (Enumeration e = UIManager.getDefaults().keys(); e.hasMoreElements();) {
                    Object o = e.nextElement();
                    if (o.toString().indexOf(".font") != -1)
                        UIManager.getDefaults().put(o,
                                defFont);
                    //System.out.println(e.nextElement());
                }
            } finally {

            }

            System.out.println(Thread.currentThread());
            final ZoeosFrame zf = ZoeosFrame.getInstance();
            zf.show();
            if (Preferences.userNodeForPackage(TipOfTheDayDialog.class).getBoolean(TipOfTheDayDialog.PREF_showTipsAtStartup, true) == true)
                zf.showTips();

            final Preferences prefs = Preferences.userRoot().node("/com/pcmsolutions/system/zoeos");
            if (ZoeosPreferences.ZPREF_autoHuntAtStartup.getValue() == true)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        new Thread() {
                            public void run() {
                                try {
                                    Zoeos.getInstance().getDeviceManager().performHunt();
                                } finally {
                                }
                            }
                        }.start();
                    }
                });
        } catch (OutOfMemoryError e) {
            System.out.println(e);
            System.exit(-1);
        }
    }

    private static void initLook() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

        com.jidesoft.utils.Lm.verifyLicense("Zuonics", "ZoeOS", "odQ6AtjgQBLkRNQQNqiFlMcc2lIq1Pe2");

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.licenseCode", "a#PCM_Solutions_Ltd#1pj3nkw#a522i8");
        com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.isLookAndFeelFrameDecoration", "true");
        AlloyLookAndFeel.setProperty("alloy.isButtonPulseEffectEnabled", "false");


        //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.glass.GlassTheme(new CustomFontTheme(new String[]{"Arial", "Courier"}, 10, 9));
        //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.acid.AcidTheme(new CustomFontTheme(new String[]{"Arial", "Courier"}, 10, 9));
        //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.glass.GlassTheme();
        //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.bedouin.BedouinTheme(new CustomFontTheme(new String[]{"Arial", "Courier"}, 11, 11));
        //                com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.bedouin.BedouinTheme(new CustomFontTheme(new String[]{"Microsoft Sans Serif", "Arial"}, 11, 11));

        com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.bedouin.BedouinTheme(new AlloyFontTheme() {
            //com.incors.plaf.alloy.AlloyTheme theme = new com.incors.plaf.alloy.themes.acid.AcidTheme(new AlloyFontTheme() {
            public FontUIResource getControlTextFont() {
                return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
            }

            public FontUIResource getSystemTextFont() {
                return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
            }

            public FontUIResource getUserTextFont() {
                return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
            }

            public FontUIResource getMenuTextFont() {
                return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 11);
            }

            public FontUIResource getWindowTitleFont() {
                return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
            }

            public FontUIResource getSubTextFont() {
                return new FontUIResource("Microsoft Sans Serif", Font.PLAIN, 10);
            }
        });

        javax.swing.LookAndFeel alloyLnF = new com.incors.plaf.alloy.AlloyLookAndFeel(theme);
        //AlloyJideLookAndFeel ajlf = new AlloyJideLookAndFeel();
        //ajlf.setTheme(theme, true);
        javax.swing.UIManager.setLookAndFeel(alloyLnF);
        //javax.swing.UIManager.setLookAndFeel(ajlf);


        Font defFont = new Font("Arial", Font.PLAIN, 11);
        // UIManager.getDefaults().put("Label.font", defFont);

        //using the put keys to set the font size for the different visual components.
        for (Enumeration e = UIManager.getDefaults().keys(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o.toString().indexOf(".font") != -1)
                UIManager.getDefaults().put(o,
                        defFont);
            //System.out.println(e.nextElement());
        }

        UIManager.getDefaults().put("Button.font",
                defFont);
        /* UIManager.getDefaults().put("JButton.font",
        new Font(MmiConstants.DEFAULT_MMI_FONT, 0, fontSize));*/
        UIManager.getDefaults().put("Label.font",
                defFont);
        UIManager.getDefaults().put("TextField.font",
                defFont);
        UIManager.getDefaults().put("TextArea.font",
                defFont);
        UIManager.getDefaults().put("RadioButton.font",
                defFont);
        UIManager.getDefaults().put("CheckBox.font",
                defFont);
        UIManager.getDefaults().put("TabbedPane.font",
                defFont);
        UIManager.getDefaults().put("TitledBorder.font",
                defFont);
        UIManager.getDefaults().put("Spinner.font",
                defFont);
        UIManager.getDefaults().put("FormattedTextField",
                defFont);
        UIManager.getDefaults().put("JFormattedTextField",
                defFont);
        UIManager.getDefaults().put("AbstractButton",
                defFont);
    }

}
