
import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.jidesoft.action.DockableBarContext;
import com.jidesoft.action.DockableBarDockableHolder;
import com.jidesoft.action.event.CommandMenuBar;
import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.office2003.Office2003Painter;
import com.jidesoft.plaf.office2003.Office2003Theme;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideSplitButton;
import com.jidesoft.utils.Lm;
import com.jidesoft.utils.SystemInfo;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;

/**
 */
public class VsnetCommandBarFactory extends CommandBarFactory {
    private static boolean _autohideAll = false;
    private static String _fullScreenLayout;

    public static CommandBar createMenuCommandBar(DockableBarDockableHolder frame) {
        CommandBar commandBar = new CommandMenuBar("Menu Bar");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.getContext().setInitIndex(0);
        commandBar.setPaintBackground(false);
        commandBar.setStretch(true);
        commandBar.setFloatable(true);
        commandBar.setHidable(false);

        commandBar.add(createFileMenu());
        addDemoMenus(commandBar, new String[]{"Edit"});
        commandBar.add(createViewMenu(frame));
        commandBar.add(createOptionMenu(frame));
        addDemoMenus(commandBar, new String[]{"Project", "Build", "Debug", "Tools"});
        commandBar.add(createWindowsMenu(frame));
        addDemoMenus(commandBar, new String[]{"Help"});
        commandBar.add(createLookAndFeelMenu(frame));

        return commandBar;
    }

    private static JMenu createWindowsMenu(final DockableBarDockableHolder frame) {
        JMenu menu = new JideMenu("Window");
        menu.setMnemonic('W');

        JMenuItem item = null;

        item = new JMenuItem("Load Default Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (frame instanceof DefaultDockableBarDockableHolder) {
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().loadLayoutData();
                }
            }
        });
        menu.add(item);

        item = new JMenuItem("Load Design Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (frame instanceof DefaultDockableBarDockableHolder) {
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().loadLayoutDataFrom("design");
                }
            }
        });
        menu.add(item);

        item = new JMenuItem("Load Debug Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (frame instanceof DefaultDockableBarDockableHolder) {
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().loadLayoutDataFrom("debug");
                }
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Save as Default Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (frame instanceof DefaultDockableBarDockableHolder) {
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().saveLayoutData();
                }
            }
        });
        menu.add(item);

        item = new JMenuItem("Save as Design Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (frame instanceof DefaultDockableBarDockableHolder) {
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().saveLayoutDataAs("design");
                }
            }
        });
        menu.add(item);

        item = new JMenuItem("Save as Debug Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (frame instanceof DefaultDockableBarDockableHolder) {
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().saveLayoutDataAs("debug");
                }
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Reset Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (frame instanceof DefaultDockableBarDockableHolder) {
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().setUseFrameBounds(false);
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().setUseFrameState(false);
                    ((DefaultDockableBarDockableHolder) frame).getLayoutPersistence().resetToDefault();
                }
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Toggle Auto Hide All");
        item.setMnemonic('T');
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!_autohideAll) {
                    _fullScreenLayout = frame.getDockingManager().getLayoutData();
                    frame.getDockingManager().autohideAll();
                    _autohideAll = true;
                }
                else {
                    // call next two methods so that the farme bounds and state will not change.
                    frame.getDockingManager().setUseFrameBounds(false);
                    frame.getDockingManager().setUseFrameState(false);
                    if (_fullScreenLayout != null) {
                        frame.getDockingManager().setLayoutData(_fullScreenLayout);
                    }
                    _autohideAll = false;
                }
            }
        });
        menu.add(item);

        return menu;
    }

    private static JMenu createOptionMenu(final DockableBarDockableHolder frame) {
        JMenu menu = new JideMenu("Options");
        menu.setMnemonic('P');

        JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem("Frames Floatable");
        checkBoxMenuItem.setMnemonic('F');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    frame.getDockingManager().setFloatable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isFloatable());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Autohidable");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    frame.getDockingManager().setAutohidable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isAutohidable());

        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Hidable");
        checkBoxMenuItem.setMnemonic('H');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    frame.getDockingManager().setHidable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isHidable());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Rearrangable");
        checkBoxMenuItem.setMnemonic('R');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    frame.getDockingManager().setRearrangable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isHidable());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Resizable");
        checkBoxMenuItem.setMnemonic('S');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    frame.getDockingManager().setResizable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isResizable());
        menu.add(checkBoxMenuItem);

        menu.addSeparator();

        JMenu buttonsMenu = new JMenu("Available Titlebar Buttons");

        checkBoxMenuItem = new JCheckBoxMenuItem("Close Button");
        checkBoxMenuItem.setMnemonic('C');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(frame, selected, DockableFrame.BUTTON_CLOSE);
            }
        });
        checkBoxMenuItem.setSelected(true);
        buttonsMenu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Autohide Button");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(frame, selected, DockableFrame.BUTTON_AUTOHIDE);
            }
        });
        checkBoxMenuItem.setSelected(true);
        buttonsMenu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Float Button");
        checkBoxMenuItem.setMnemonic('F');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(frame, selected, DockableFrame.BUTTON_FLOATING);
            }
        });
        checkBoxMenuItem.setSelected(true);
        buttonsMenu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Maximize Button");
        checkBoxMenuItem.setMnemonic('M');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(frame, selected, DockableFrame.BUTTON_MAXIMIZE);
            }
        });
        checkBoxMenuItem.setSelected(false);
        buttonsMenu.add(checkBoxMenuItem);

        menu.add(buttonsMenu);

        menu.addSeparator();

        checkBoxMenuItem = new JCheckBoxMenuItem("Continuous Layout");
        checkBoxMenuItem.setMnemonic('C');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    frame.getDockingManager().setContinuousLayout(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (frame.getDockingManager().isContinuousLayout()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<B><FONT FACE='Tahoma' SIZE='4' COLOR='#0000FF'>Continuous Layout</FONT></B><FONT FACE='Tahoma'>" +
                                "<FONT FACE='Tahoma' SIZE='3'><BR><BR><B>An option to continuously layout affected components during resizing." +
                                "<BR></B><BR>This is the same option as in JSplitPane. If the option is true, when you resize" +
                                "<BR>the JSplitPane's divider, it will continuously redisplay and laid out during user" +
                                "<BR>intervention." +
                                "<BR><BR>Default: off</FONT>" +
                                "<BR></HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isContinuousLayout());
        menu.add(checkBoxMenuItem);

        menu.addSeparator();

        checkBoxMenuItem = new JCheckBoxMenuItem("Easy Tab Docking");
        checkBoxMenuItem.setMnemonic('E');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    frame.getDockingManager().setEasyTabDock(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (frame.getDockingManager().isEasyTabDock()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<B><FONT COLOR='BLUE' FACE='Tahoma' SIZE='4'>Easy Tab Docking </FONT></B>" +
                                "<BR><BR><FONT FACE='Tahoma' SIZE='3'><B>An option to make the tab-docking of a dockable frame easier</B>" +
                                "<BR><BR>It used to be dragging a dockable frame and pointing to the title" +
                                "<BR>bar of another dockable frame to tab-dock with it. However if " +
                                "<BR>this option on, pointing to the middle portion of any dockable " +
                                "<BR>frame will tab-dock with that frame." +
                                "<BR><BR>Default: off</FONT>" +
                                "<BR></HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isEasyTabDock());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Allow Nested Floating Windows");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    frame.getDockingManager().setNestedFloatingAllowed(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (frame.getDockingManager().isNestedFloatingAllowed()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<FONT FACE='Tahoma' SIZE='4'><B><FONT COLOR='#0000FF'>Nested Floating Windows<BR></FONT></B><BR></FONT>" +
                                "<FONT FACE='Tahoma' SIZE='3'><B>An option to allow nested windows when in floating mode</B>" +
                                "<BR><BR>JIDE Docking Framework can allow you to have as many nested windows in one floating " +
                                "<BR>container as you want. However, not all your users want to have that complexity. So we " +
                                "<BR>leave it as an option and you can choose to turn it on or off. " +
                                "<BR><BR>Default: off</FONT> <BR>" +
                                "</HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isNestedFloatingAllowed());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Show Gripper");
        checkBoxMenuItem.setMnemonic('S');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    frame.getDockingManager().setShowGripper(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (frame.getDockingManager().isShowGripper()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<FONT FACE='Tahoma' SIZE='4'><FONT COLOR='#0000FF'><B>Show Gripper</B><BR></FONT><BR></FONT>" +
                                "<FONT FACE='Tahoma' SIZE='3'><B>An option to give user a visual hint that the dockable frame can be dragged<BR></B>" +
                                "<BR>Normal tabs in JTabbedPane can not be dragged. However in our demo, " +
                                "<BR>most of them can be dragged. To make it obvious to user, we added an " +
                                "<BR>option so that a gripper is painted on the tab or the title bar of those " +
                                "<BR>dockable frames which can be dragged." +
                                "<BR><BR>Default: off</FONT><BR>" +
                                "</HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(true);//frame.getDockingManager().isShowGripper());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Show TitleBar");
        checkBoxMenuItem.setMnemonic('T');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    frame.getDockingManager().setShowTitleBar(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (frame.getDockingManager().isShowTitleBar()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<FONT FACE='Tahoma' SIZE='4'><FONT COLOR='#0000FF'><B>Show TitleBar</B><BR></FONT><BR></FONT>" +
                                "<FONT FACE='Tahoma' SIZE='3'><B>An option to show/hide dockable frame's title bar<BR></B>" +
                                "<BR><BR>Default: on</FONT><BR>" +
                                "</HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isShowTitleBar());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("SideBar Rollover");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new AbstractAction() {
            public void actionPerformed
                    (ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    frame.getDockingManager().setSidebarRollover(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (frame.getDockingManager().isSidebarRollover()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<FONT FACE='Tahoma' SIZE='4'><FONT COLOR='#0000FF'><B>SideBar Rollover</B><BR></FONT><BR></FONT>" +
                                "<FONT FACE='Tahoma' SIZE='3'><B>An option to control the sensibility of tabs on sidebar<BR></B>" +
                                "<BR>Each tab on four sidebars is corresponding to a dockable frame. Usually when " +
                                "<BR>user moves mouse over the tab, the dockable frame will show up. However in Eclipse" +
                                "<BR>you must click on it to show the dockable frame. This option will allow you to " +
                                "<BR>control the sensibility of it." +
                                "<BR><BR>Default: on</FONT><BR>" +
                                "</HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(frame.getDockingManager().isSidebarRollover());
        menu.add(checkBoxMenuItem);

        menu.addSeparator();

        JRadioButtonMenuItem radioButtonMenuItem1 = new JRadioButtonMenuItem("Draw Full Outline When Dragging");
        radioButtonMenuItem1.setMnemonic('D');
        radioButtonMenuItem1.addActionListener(new ActionListener() {
            public void actionPerformed
                    (ActionEvent
                    e) {
                if (e.getSource() instanceof JRadioButtonMenuItem) {
                    frame.getDockingManager().setOutlineMode(DefaultDockingManager.FULL_OUTLINE_MODE);
                    Lm.showPopupMessageBox("<HTML>" +
                            "<B><FONT FACE='Tahoma' SIZE='4' COLOR='#0000FF'>Outline Paint Mode</FONT></B><FONT FACE='Tahoma'>" +
                            "<FONT SIZE='4'>" +
                            "<FONT COLOR='#0000FF' SIZE='3'><BR><BR><B>An option of how to paint the outline during dragging.</B></FONT>" +
                            "<BR><BR><FONT SIZE='3'>Since our demo is purely based on Swing, and there is no way to have transparent native " +
                            "<BR>window using Swing. So we have to develop workarounds to paint the outline of a dragging frame. " +
                            "<BR>As a result, we get two ways to draw the outline. Since neither is perfect, we just leave it as " +
                            "<BR>an option to user to choose. You can try each of the option and see which one you like most." +
                            "<BR><B><BR>Option 1: PARTIAL_OUTLINE_MODE</B><BR>Pros: Fast, very smooth, works the best if user " +
                            "of your application always keeps it as full screen" +
                            "<BR>Cons: Partial outline or no outline at all if outside main frame although it's there wherever " +
                            "your mouse is." +
                            "<BR><BR><B>Option 2: FULL_OUTLINE_MODE</B>" +
                            "<BR>Pros: It always draw the full outline" +
                            "<BR>Cons: Sometimes it's flickering. Slower comparing with partial outline mode." +
                            "<BR><BR>Default: PARTIAL_OUTLINE_MODE</FONT>" +
                            "<BR></HTML>");
                }
            }
        });
        radioButtonMenuItem1.setSelected(frame.getDockingManager().getOutlineMode() == DefaultDockingManager.FULL_OUTLINE_MODE);
        menu.add(radioButtonMenuItem1);

        JRadioButtonMenuItem radioButtonMenuItem2 = new JRadioButtonMenuItem("Draw Partial Outline When Dragging");
        radioButtonMenuItem2.setMnemonic('P');
        radioButtonMenuItem2.addActionListener(new ActionListener() {
            public void actionPerformed
                    (ActionEvent
                    e) {
                if (e.getSource() instanceof JRadioButtonMenuItem) {
                    frame.getDockingManager().setOutlineMode(DefaultDockingManager.PARTIAL_OUTLINE_MODE);
                    Lm.showPopupMessageBox("<HTML>" +
                            "<B><FONT FACE='Tahoma' SIZE='4' COLOR='#0000FF'>Outline Paint Mode</FONT></B><FONT FACE='Tahoma'>" +
                            "<FONT SIZE='4'><FONT COLOR='#0000FF'><BR></FONT><BR></FONT><B>An option of how to paint the outline during dragging. " +
                            "<BR><BR></B>Since our demo is purely based on Swing, and there is no way to have transparent native " +
                            "<BR>window using Swing. So we have to develop workarounds to paint the outline of a dragging frame. " +
                            "<BR>As a result, we get two ways to draw the outline. Since neither is perfect, we just leave it as " +
                            "<BR>an option to user to choose. You can try each of the option and see which one you like most." +
                            "<BR><B><BR>Option 1: PARTIAL_OUTLINE_MODE</B>" +
                            "<BR>Pros: Fast, very smooth" +
                            "<BR>Cons: Partial outline or no outline at all if outside main frame although it&#39;s there wherever your mouse is." +
                            "<BR><BR><B>Option 2: FULL_OUTLINE_MODE</B>" +
                            "<BR>Pros: It always draw the full outline<BR>Cons: Sometimes it&#39;s flickering. Slower comparing with partial outline mode.</FONT>" +
                            "<BR><BR><FONT FACE='Tahoma'>Default: PARTIAL_OUTLINE_MODE</FONT>" +
                            "<BR></HTML>");
                }
            }
        });
        radioButtonMenuItem2.setSelected(frame.getDockingManager().getOutlineMode() == DefaultDockingManager.PARTIAL_OUTLINE_MODE);
        menu.add(radioButtonMenuItem2);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButtonMenuItem1);
        buttonGroup.add(radioButtonMenuItem2);

        return menu;
    }

    private static void toggleButton(DockableBarDockableHolder frame, boolean selected, int button) {
        Collection names = frame.getDockingManager().getAllFrames();
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            DockableFrame f = frame.getDockingManager().getFrame(name);
            if (selected) {
                f.setAvailableButtons(f.getAvailableButtons() | button);
            }
            else {
                f.setAvailableButtons(f.getAvailableButtons() & ~button);
            }
        }
    }

    static JMenu createLookAndFeelMenu(final DockableBarDockableHolder frame) {
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
                frame.getDockableBarManager().updateComponentTreeUI();
                frame.getDockingManager().updateComponentTreeUI();
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
                frame.getDockableBarManager().updateComponentTreeUI();
                frame.getDockingManager().updateComponentTreeUI();
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
                    frame.getDockableBarManager().updateComponentTreeUI();
                    frame.getDockingManager().updateComponentTreeUI();
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
                frame.getDockableBarManager().updateComponentTreeUI();
                frame.getDockingManager().updateComponentTreeUI();
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
                frame.getDockableBarManager().updateComponentTreeUI();
                frame.getDockingManager().updateComponentTreeUI();
            }
        });
        menu.add(item);

        item = new JMenuItem("Aqua LookAndFeel");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.AQUA_LNF);
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
                frame.getDockableBarManager().updateComponentTreeUI();
                frame.getDockingManager().updateComponentTreeUI();
            }
        });
        item.setEnabled(SystemInfo.isMacOSX());
        menu.add(item);

        return menu;
    }

    protected static CommandBar createStandardCommandBar() {
        CommandBar commandBar = new CommandBar("Standard");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.getContext().setCurrentMode(DockableBarContext.STATE_HORI_DOCKED);
        commandBar.getContext().setInitIndex(1);

        commandBar.add(createSplitButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.ADD_NEW_ITEMS)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.OPEN)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SAVE)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SAVE_ALL)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.CUT)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.COPY)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.PASTE)));
        commandBar.addSeparator();

        commandBar.add(createSplitButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.UNDO)));
        commandBar.add(createSplitButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.REDO)));
        commandBar.add(createSplitButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.NAVIGATE_BACKWARD)));
        commandBar.add(createSplitButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.NAVIGATE_FORWARD)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.START)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FIND_IN_FILES)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SOLUTION)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.PROPERTY)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.TOOLBOX)));
        JideSplitButton splitButton = (JideSplitButton) commandBar.add(createSplitButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.CLASSVIEW)));

        JMenuItem item = (JMenuItem) splitButton.add(new JMenuItem("Class View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.CLASSVIEW)));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        splitButton.add(new JMenuItem("Server Explorer", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SERVER)));
        splitButton.add(new JMenuItem("Resource View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.RESOURCEVIEW)));
        splitButton.addSeparator();

        splitButton.add(new JMenuItem("Macro Explorer", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.MACRO)));
        splitButton.add(new JMenuItem("Object Browser", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.OBJECT)));
        splitButton.add(new JMenuItem("Document Outline", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.DOCUMENTOUTLINE)));
        splitButton.addSeparator();

        splitButton.add(new JMenuItem("Task List", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.TASKLIST)));
        splitButton.add(new JMenuItem("Command Window", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.COMMAND)));
        splitButton.add(new JMenuItem("Output", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.OUTPUT)));
        splitButton.addSeparator();

        splitButton.add(new JMenuItem("Find Result 1", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FINDRESULT1)));
        splitButton.add(new JMenuItem("Find Result 2", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FINDRESULT2)));
        splitButton.add(new JMenuItem("Find Symbol", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FINDSYMBOL)));
        splitButton.addSeparator();

        splitButton.add(new JMenuItem("Favorites", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FAVORITES)));

        return commandBar;
    }

    private static JMenu createFileMenu() {
        JMenuItem item;

        JMenu fileMenu = new JideMenu("File");
        fileMenu.setMnemonic('F');

        item = new JMenuItem("New", 'N');
        fileMenu.add(item);

        item = new JMenuItem("Open...", 'O');
        fileMenu.add(item);

        item = new JMenuItem("Close", 'C');
        fileMenu.add(item);

        fileMenu.addSeparator();

        item = new JMenuItem("Exit", 'x');
        fileMenu.add(item);
        return fileMenu;
    }

    protected static CommandBar createBuildCommandBar() {
        CommandBar commandBar = new CommandBar("Build");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.getContext().setCurrentMode(DockableBarContext.STATE_HORI_DOCKED);
        commandBar.getContext().setInitIndex(1);

        JideButton button = (JideButton) commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Build.BUILD_FILE)));
        button.setEnabled(false);
        button = (JideButton) commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Build.BUILD_SOLUTION)));
        button.setEnabled(false);
        button = (JideButton) commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Build.CANCEL)));
        button.setEnabled(false);

        return commandBar;
    }

    public static CommandBar createLayoutCommandBar() {
        CommandBar commandBar = new CommandBar("Layout");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.getContext().setCurrentMode(DockableBarContext.STATE_HORI_DOCKED);
        commandBar.getContext().setInitIndex(2);

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.ALIGN_TO_GRID)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.ALIGN_LEFTS)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.ALIGN_CENTERS)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.ALIGN_RIGHTS)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.ALIGN_TOPS)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.ALIGN_MIDDLES)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.ALIGN_BOTTOMS)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.MAKE_SAME_WIDTH)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.SIZE_TO_GRID)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.MAKE_SAME_HEIGHT)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.MAKE_SAME_SIZE)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.MAKE_HORI_SPACING_EQUAL)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.INC_HORI_SPACING)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.DEC_HORI_SPACING)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.REMOVE_HORI_SPACING)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.MAKE_VERT_SPACING_EQUAL)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.INC_VERT_SPACING)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.DEC_VERT_SPACING)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.REMOVE_VERT_SPACING)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.CENTER_HORI)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.CENTER_VERT)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.BRING_TO_FRONT)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Layout.SEND_TO_BACK)));

        return commandBar;
    }

    public static CommandBar createFormattingCommandBar() {
        CommandBar commandBar = new CommandBar("Formatting");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
        commandBar.getContext().setCurrentMode(DockableBarContext.STATE_HORI_DOCKED);
        commandBar.getContext().setInitIndex(0);

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.BOLD)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.ITALIC)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.UNDERLINE)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.FOREGROUND)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.BACKGROUND)));
        commandBar.addSeparator();

        ButtonGroup alignmentGroup = new ButtonGroup();
        JideButton button = (JideButton) commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.ALIGN_LEFT)));
        button.setSelected(true);
        alignmentGroup.add(button);
        button = (JideButton) commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.ALIGN_CENTER)));
        alignmentGroup.add(button);
        button = (JideButton) commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.ALIGN_RIGHT)));
        alignmentGroup.add(button);
        button = (JideButton) commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.JUSTIFY)));
        alignmentGroup.add(button);
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.NUMBERING)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.BULLETS)));
        commandBar.addSeparator();

        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.DECREASE_INDENT)));
        commandBar.add(createButton(VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Formatting.INCREASE_INDENT)));
        return commandBar;
    }

    public static JMenu createViewMenu(final DockableBarDockableHolder frame) {
        JMenuItem item;
        JMenu viewMenu = new JideMenu("View");
        viewMenu.setMnemonic('V');

        item = new JMenuItem("Project View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SOLUTION));
        item.setMnemonic('P');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Project View");
            }
        });
        viewMenu.add(item);

        item = new JMenuItem("Class View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.CLASSVIEW));
        item.setMnemonic('A');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Class View");
            }
        });
        viewMenu.add(item);

        item = new JMenuItem("Server Explorer", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SERVER));
        item.setMnemonic('V');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Server Explorer");
            }
        });
        viewMenu.add(item);

        item = new JMenuItem("Resource View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.RESOURCEVIEW));
        item.setMnemonic('R');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Resource View");
            }
        });
        viewMenu.add(item);

        item = new JMenuItem("Properties Window", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.PROPERTY));
        item.setMnemonic('W');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Property");
            }
        });
        viewMenu.add(item);

        viewMenu.addSeparator();

        item = new JMenuItem("Task List", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.TASKLIST));
        item.setMnemonic('T');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Task List");
            }
        });
        viewMenu.add(item);

        item = new JMenuItem("Command Window", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.COMMAND));
        item.setMnemonic('N');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Command");
            }
        });
        viewMenu.add(item);

        item = new JMenuItem("Output", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.OUTPUT));
        item.setMnemonic('U');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Output");
            }
        });
        viewMenu.add(item);

        viewMenu.addSeparator();

        item = new JMenuItem("Find Results 1", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FINDRESULT1));
        item.setMnemonic('1');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Find Results 1");
            }
        });
        viewMenu.add(item);

        item = new JMenuItem("Find Results 2", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FINDRESULT2));
        item.setMnemonic('2');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getDockingManager().showFrame("Find Results 2");
            }
        });
        viewMenu.add(item);

        return viewMenu;
    }
}
