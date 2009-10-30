/*
 * @(#)TwoDockingFrameworkDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software. All rights reserved.
 */

import com.jidesoft.action.*;
import com.jidesoft.action.event.CommandMenuBar;
import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.DockableHolder;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.office2003.Office2003Painter;
import com.jidesoft.plaf.office2003.Office2003Theme;
import com.jidesoft.swing.*;
import com.jidesoft.utils.Lm;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * This is a sample program for JIDE Action Framework. It will create a JFrame which is a DefaultDockableBarHolder and
 * a tabbed pane with two tabs. In each tab, it's a panel which has its own DockableBar and DockableFrame.
 * <br>
 * Required jar files: jide-common.jar, jide-dock.jar, jide-action.jar, jide_grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class ThreeActionFrameworkDemo extends DefaultDockableBarHolder {

    private static ThreeActionFrameworkDemo _frame;

    private static final String PROFILE_NAME = "jidesoft-threeactions";

    private static WindowAdapter _windowListener;

    static JideTabbedPane _tabbedPane;

    static DockableHolderPanel _firstPanel;
    static DockableHolderPanel _secondPanel;

    public ThreeActionFrameworkDemo(String title) throws HeadlessException {
        super(title);
    }

    public DockingManager getDockingManager() {
        if (_tabbedPane == null) {
            return null;
        }
        int selected = _tabbedPane.getSelectedIndex();
        if (selected == 0) {
            return _firstPanel.getDockingManager();
        }
        else {
            return _secondPanel.getDockingManager();
        }
    }

    public LayoutPersistence getLayoutPersistence() {
        if (_tabbedPane == null) {
            return null;
        }
        int selected = _tabbedPane.getSelectedIndex();
        if (selected == 0) {
            return _firstPanel.getLayoutPersistence();
        }
        else {
            return _secondPanel.getLayoutPersistence();
        }
    }

    static class DockableHolderPanel extends ContentContainer implements DockableBarHolder, DockableHolder {
        private DockingManager _dockingManager;
        private DockableBarManager _dockableBarManager;
        private LayoutPersistenceManager _layoutPersistenceManager;

        public DockableHolderPanel(JFrame parent) {
            _dockableBarManager = new DefaultDockableBarManager(parent, this);
            _dockingManager = new DefaultDockingManager(parent, _dockableBarManager.getMainContainer());
            _layoutPersistenceManager = new LayoutPersistenceManager();
            _layoutPersistenceManager.setProfileKey(PROFILE_NAME);
            _layoutPersistenceManager.addLayoutPersistence(_dockableBarManager);
            _layoutPersistenceManager.addLayoutPersistence(_dockingManager);
        }

        public DockingManager getDockingManager() {
            return _dockingManager;
        }

        public DockableBarManager getDockableBarManager() {
            return _dockableBarManager;
        }

        public LayoutPersistence getLayoutPersistence() {
            return _layoutPersistenceManager;
        }
    }

    static String getPrefix() {
        if (_tabbedPane.getSelectedIndex() == 0) {
            return "app1_";
        }
        else if (_tabbedPane.getSelectedIndex() == 1) {
            return "app2_";
        }
        return "";
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (UnsupportedLookAndFeelException e) {
        }
        LookAndFeelFactory.installJideExtension();

        _frame = new ThreeActionFrameworkDemo("Demo of JIDE Action Framework - three DockableBarManagers under the same JFrame");
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());

        Lm.setParent(_frame);

        // add a widnow listener so that timer can be stopped when exit
        _windowListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                clearUp();
            }
        };
        _frame.addWindowListener(_windowListener);

        _firstPanel = new DockableHolderPanel(_frame);
        _secondPanel = new DockableHolderPanel(_frame);

        _tabbedPane = new JideTabbedPane();
        _frame.getDockableBarManager().getMainContainer().setLayout(new BorderLayout());
        _frame.getDockableBarManager().getMainContainer().add(_tabbedPane, BorderLayout.CENTER);

        _tabbedPane.addTab("DockableBarManager 1", _firstPanel);
        _tabbedPane.addTab("DockableBarManager 2", _secondPanel);
        _tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                switchDockableBarManager(_tabbedPane.getSelectedIndex());
            }
        });

        _frame.getDockableBarManager().setProfileKey(PROFILE_NAME);
        _frame.getDockableBarManager().addDockableBar(createMenuBar());
        _frame.getDockableBarManager().addDockableBar(Office2003CommandBarFactory.createStandardCommandBar());
        _frame.getDockableBarManager().addDockableBar(Office2003CommandBarFactory.createDrawingCommandBar());

        _firstPanel.getDockableBarManager().addDockableBar(Office2003CommandBarFactory.createFormattingCommandBar());
        _firstPanel.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createLayoutCommandBar());
        _firstPanel.getDockingManager().addFrame(DockableFrameFactory.createSampleResourceViewFrame());
        _firstPanel.getDockingManager().addFrame(DockableFrameFactory.createSampleClassViewFrame());


        _secondPanel.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createStandardCommandBar());
        _secondPanel.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createFormattingCommandBar());
        _secondPanel.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createBuildCommandBar());
        _secondPanel.getDockingManager().beginLoadLayoutData();
        _secondPanel.getDockingManager().addFrame(DockableFrameFactory.createSampleProjectViewFrame());
        _secondPanel.getDockingManager().addFrame(DockableFrameFactory.createSampleServerFrame());
        _secondPanel.getDockingManager().addFrame(DockableFrameFactory.createSamplePropertyFrame());

        _frame.getDockableBarManager().loadLayoutData();

        _firstPanel.getLayoutPersistence().loadLayoutDataFrom("first_default");
        _secondPanel.getLayoutPersistence().loadLayoutDataFrom("second_default");

        switchDockableBarManager(_tabbedPane.getSelectedIndex());

        _frame.toFront();
    }

    private static void switchDockableBarManager(int index) {
        if (index == 0) {
            _firstPanel.getDockableBarManager().setFloatingDockableBarsVisible(true);
            _firstPanel.getDockingManager().setFloatingFramesVisible(true);
        }
        else {
            _firstPanel.getDockingManager().hideActiveAutohideFrame();
            _firstPanel.getDockingManager().setFloatingFramesVisible(false);
            _firstPanel.getDockableBarManager().setFloatingDockableBarsVisible(false);
        }

        if (index == 1) {
            _secondPanel.getDockableBarManager().setFloatingDockableBarsVisible(true);
            _secondPanel.getDockingManager().setFloatingFramesVisible(true);
        }
        else {
            _secondPanel.getDockingManager().hideActiveAutohideFrame();
            _secondPanel.getDockingManager().setFloatingFramesVisible(false);
            _secondPanel.getDockableBarManager().setFloatingDockableBarsVisible(false);
        }
    }

    private static void clearUp() {
        _frame.removeWindowListener(_windowListener);
        _windowListener = null;

        if (_firstPanel.getLayoutPersistence() != null) {
            _firstPanel.getLayoutPersistence().saveLayoutDataAs("first_default");
        }
        if (_secondPanel.getLayoutPersistence() != null) {
            _secondPanel.getLayoutPersistence().saveLayoutDataAs("second_default");
        }

        _frame.getDockableBarManager().saveLayoutData();

        _frame.dispose();
        Lm.setParent(null);
        _frame = null;
    }

    protected static CommandBar createMenuBar() {
        CommandBar commandBar = new CommandMenuBar("Menu Bar");
        commandBar.setStretch(true);
        commandBar.setPaintBackground(false);

        JMenu fileMenu = createFileMenu();
        JMenu viewMenu = createViewMenu();
        JMenu windowMenu = createWindowsMenu();
        JMenu lnfMenu = createLookAndFeelMenu();
        JMenu helpMenu = createHelpMenu();

        commandBar.add(fileMenu);
        commandBar.add(viewMenu);
        commandBar.add(windowMenu);
        commandBar.add(lnfMenu);
        commandBar.add(helpMenu);

        return commandBar;
    }

    private static JScrollPane createScrollPane(Component component) {
        JScrollPane pane = new JideScrollPane(component);
        return pane;
    }

    private static JMenu createHelpMenu() {
        JMenu menu = new JideMenu("Help");
        menu.setMnemonic('H');

        JMenuItem item = new JMenuItem("About JIDE Products");
        item.setMnemonic('A');
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Lm.showAboutMessageBox();
            }
        });
        menu.add(item);

        return menu;
    }

    private static JMenu createWindowsMenu() {
        JMenu menu = new JideMenu("Window");
        menu.setMnemonic('W');

        JMenuItem item = new JMenuItem("Load Default Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getLayoutPersistence().loadLayoutDataFrom(getPrefix() + "default");
            }
        });
        menu.add(item);

        item = new JMenuItem("Load Design Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
//use these two lines if you don't want the frame size and state changes
//                _frame.getDockingManager().setUseFrameBounds(false);
//                _frame.getDockingManager().setUseFrameState(false);
                _frame.getLayoutPersistence().loadLayoutDataFrom(getPrefix() + "design");
            }
        });
        menu.add(item);

        item = new JMenuItem("Load Debug Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getLayoutPersistence().loadLayoutDataFrom(getPrefix() + "debug");
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Save as Default Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getLayoutPersistence().saveLayoutDataAs(getPrefix() + "default");
            }
        });
        menu.add(item);

        item = new JMenuItem("Save as Design Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getLayoutPersistence().saveLayoutDataAs(getPrefix() + "design");
            }
        });
        menu.add(item);

        item = new JMenuItem("Save as Debug Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getLayoutPersistence().saveLayoutDataAs(getPrefix() + "debug");
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Reset Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getLayoutPersistence().setUseFrameBounds(false);
                _frame.getLayoutPersistence().setUseFrameState(false);
                _frame.getLayoutPersistence().resetToDefault();
            }
        });
        menu.add(item);
        return menu;
    }

    private static JMenu createViewMenu() {
        JMenuItem item;
        JMenu menu = new JideMenu("View");
        menu.setMnemonic('V');

        item = new JMenuItem("Project View", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME1));
        item.setMnemonic('P');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Project View");
            }
        });
        menu.add(item);

        item = new JMenuItem("Class View", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME2));
        item.setMnemonic('A');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Class View");
            }
        });
        menu.add(item);

        item = new JMenuItem("Server Explorer", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME3));
        item.setMnemonic('V');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Server Explorer");
            }
        });
        menu.add(item);

        item = new JMenuItem("Resource View", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME4));
        item.setMnemonic('R');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Resource View");
            }
        });
        menu.add(item);

        item = new JMenuItem("Properties Window", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME5));
        item.setMnemonic('W');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Property");
            }
        });
        menu.add(item);

        menu.addSeparator();

        return menu;
    }

    private static JMenu createFileMenu() {
        JMenuItem item;

        JMenu menu = new JideMenu("File");
        menu.setMnemonic('F');

        item = new JMenuItem("Exit");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearUp();
                System.exit(0);
            }
        });
        menu.add(item);
        return menu;
    }

    private static JMenu createLookAndFeelMenu() {
        JMenuItem item;
        JMenu menu = new JideMenu("Look and Feel");
        menu.setMnemonic('L');

        item = new JMenuItem("Vsnet LookAndFeel (Windows)");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
                updateAllComponentTreeUI();
            }
        });
        menu.add(item);

        item = new JMenuItem("Office 2003 LookAndFeel (Windows)");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);
                updateAllComponentTreeUI();
            }
        });
        menu.add(item);

        JMenu themeMenu = new JideMenu("Themes (for Office 2003 L&F only)");
        Collection themes = ((Office2003Painter) Office2003Painter.getInstance()).getAvailableThemes();
        for (Iterator iterator = themes.iterator(); iterator.hasNext();) {
            Office2003Theme theme = (Office2003Theme) iterator.next();
            item = new JMenuItem(theme.getThemeName());
            item.addActionListener(new AbstractAction(theme.getThemeName()) {
                public void actionPerformed(ActionEvent e) {
                    ((Office2003Painter) Office2003Painter.getInstance()).setColorName((String) getValue(Action.NAME));
                    updateAllComponentTreeUI();
                }
            });
            themeMenu.add(item);
        }

        menu.add(themeMenu);

        menu.addSeparator();

        item = new JMenuItem("Eclipse LookAndFeel (Windows)");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.ECLIPSE_STYLE);
                updateAllComponentTreeUI();
            }
        });
        menu.add(item);

        item = new JMenuItem("Metal LookAndFeel");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(MetalLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
                updateAllComponentTreeUI();
            }
        });
        menu.add(item);

        return menu;
    }

    private static void updateAllComponentTreeUI() {
        _frame.getDockableBarManager().updateComponentTreeUI();
        _firstPanel.getDockableBarManager().updateComponentTreeUI();
        _secondPanel.getDockableBarManager().updateComponentTreeUI();
        _firstPanel.getDockingManager().updateComponentTreeUI();
        _secondPanel.getDockingManager().updateComponentTreeUI();
    }
}
