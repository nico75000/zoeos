/*
 * @(#)DockingFrameworkDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software. All rights reserved.
 */

import com.jidesoft.docking.*;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.utils.Lm;
import com.jidesoft.utils.SystemInfo;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * This is a sample program for JIDE Docking Framework. It will create a JFrame with about
 * 20 dockable frames to show the features of JIDE Docking Framework.
 * <br>
 * Required jar files: jide-common.jar, jide-dock.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class DockingFrameworkDemo extends DefaultDockableHolder {

    private static DockingFrameworkDemo _frame;

    private static final String PROFILE_NAME = "jidesoft";

    private static boolean _autohideAll = false;
    private static WindowAdapter _windowListener;
    public static JMenuItem _redoMenuItem;
    public static JMenuItem _undoMenuItem;

    public DockingFrameworkDemo(String title) throws HeadlessException {
        super(title);
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

        // Add additional UIDefault used by JIDE to UIDefault table. You have an option to specify
        // a style. If leaving it as empty, it will use VSNET_STYLE.
        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);

        _frame = new DockingFrameworkDemo("Demo of JIDE Docking Framework");
        _frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());

        // Set the parent of message boxes that come from Lm class. You don't need to call it.
        Lm.setParent(_frame);

        // add a widnow listener to do clear up when windows closing.
        _windowListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                clearUp();
            }
        };
        _frame.addWindowListener(_windowListener);

        // Set the profile key
        _frame.getDockingManager().setProfileKey(PROFILE_NAME);

// comment this if you don't want to use javax pref
//        _frame.getDockingManager().setUsePref(false);

        // Uses light-weight outline. There are several options here.
        _frame.getDockingManager().setOutlineMode(DockingManager.PARTIAL_OUTLINE_MODE);

// uncomment following lines to adjust the sliding speed of autohide frame
//        _frame.getDockingManager().setInitDelay(100);
//        _frame.getDockingManager().setSteps(1);
//        _frame.getDockingManager().setStepDelay(0);

// uncomment following lines if you want to customize the popup menu of DockableFrame
//       _frame.getDockingManager().setPopupMenuCustomizer(new com.jidesoft.docking.PopupMenuCustomizer() {
//           public void customizePopupMenu(JPopupMenu menu, DockingManager dockingManager, DockableFrame dockableFrame, boolean onTab) {
//              // ... do customization here
//           }
//
//           public boolean isPopupMenuShown(DockingManager dockingManager, DockableFrame dockableFrame, boolean onTab) {
//               return false;
//           }
//       });

// uncomment following lines if you want to customize the appearance of tabbed pane
//        DefaultDockingManager dockingManager = (DefaultDockingManager) _frame.getDockingManager();
//        dockingManager.setTabbedPaneCustomer(new DefaultDockingManager.TabbedPaneCustomizer() {
//            public void customize(JideTabbedPane tabbedPane) {
//                tabbedPane.setShrinkTabs(true);
//                tabbedPane.setTabPlacement(SwingConstants.TOP);
//            }
//        });

        // add menu bar
        _frame.setJMenuBar(createMenuBar());

        // Sets the number of steps you allow user to undo.
        _frame.getDockingManager().setUndoLimit(10);
        _frame.getDockingManager().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                refreshUndoRedoMenuItems();
            }
        });

        // Now let's start to addFrame()
        _frame.getDockingManager().beginLoadLayoutData();

        _frame.getDockingManager().setInitSplitPriority(DefaultDockingManager.SPLIT_SOUTH_NORTH_EAST_WEST);

        // add all dockable frames
        _frame.getDockingManager().addFrame(createSampleTaskListFrame());
        _frame.getDockingManager().addFrame(createSampleResourceViewFrame());
        _frame.getDockingManager().addFrame(createSampleClassViewFrame());
        _frame.getDockingManager().addFrame(createSampleProjectViewFrame());
        _frame.getDockingManager().addFrame(createSampleServerFrame());
        _frame.getDockingManager().addFrame(createSamplePropertyFrame());
        _frame.getDockingManager().addFrame(createSampleFindResult1Frame());
        _frame.getDockingManager().addFrame(createSampleFindResult2Frame());
        _frame.getDockingManager().addFrame(createSampleFindResult3Frame());
        _frame.getDockingManager().addFrame(createSampleOutputFrame());
        _frame.getDockingManager().addFrame(createSampleCommandFrame());

// just use default size. If you want to overwrite, you can call this method
//        _frame.getDockingManager().setInitBounds(new Rectangle(0, 0, 960, 800));

        // load layout information from previous session. This indicates the end of beginLoadLayoutData() method above.
        _frame.getDockingManager().loadLayoutData();

        // disallow drop dockable frame to workspace area
        _frame.getDockingManager().getWorkspace().setAcceptDockableFrame(false);

// uncomment following line(s) if you want to limit certain feature.
// If you uncomment all four lines, then the dockable frame will not
// be moved anywhere.
//        _frame.getDockingManager().setRearrangable(false);
//        _frame.getDockingManager().setAutohidable(false);
//        _frame.getDockingManager().setFloatable(false);
//        _frame.getDockingManager().setHidable(false);
        _frame.toFront();
    }

    private static void clearUp() {
        _frame.removeWindowListener(_windowListener);
        _windowListener = null;

        if (_frame.getDockingManager() != null) {
            _frame.getDockingManager().saveLayoutData();

        }

        _frame.dispose();
        Lm.setParent(null);
        _frame = null;
    }

    protected static DockableFrame createSampleProjectViewFrame() {
        DockableFrame frame = new DockableFrame("Project View", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME1));
        frame.getContext().setInitMode(DockContext.STATE_AUTOHIDE);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    protected static DockableFrame createSampleClassViewFrame() {
        DockableFrame frame = new DockableFrame("Class View", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME2));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
        frame.getContext().setInitIndex(1);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(400, 200));
        frame.setTitle("Class View - DockingFrameworkDemo");
        frame.setTabTitle("Class View");
        return frame;
    }

    protected static DockableFrame createSampleServerFrame() {
        DockableFrame frame = new DockableFrame("Server Explorer", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME3));
        frame.getContext().setInitMode(DockContext.STATE_AUTOHIDE);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
        frame.getContext().setInitIndex(0);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    protected static DockableFrame createSampleResourceViewFrame() {
        DockableFrame frame = new DockableFrame("Resource View", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME4));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
        frame.getContext().setInitIndex(1);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        frame.setTitle("Resource View");
        return frame;
    }

    protected static DockableFrame createSamplePropertyFrame() {
        DockableFrame frame = new DockableFrame("Property", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME5));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
        frame.getContext().setInitIndex(0);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    static int i = 0;

    protected static DockableFrame createSampleTaskListFrame() {
        DockableFrame frame = new DockableFrame("Task List", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME6));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        JList list = new JList(new String[]{"Task1", "Task2", "Task3"});
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String name = "Sample" + i;
                    i++;
                    DockingManager manager = _frame.getDockingManager();
                    if (manager.getFrame(name) != null) {
                        manager.showFrame(name);
                    }
                    else {
                        DockableFrame dockableFrame = new DockableFrame(name,
                                JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME1));
                        dockableFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
                        dockableFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
                        dockableFrame.getContext().setInitIndex(0);

                        dockableFrame.getContentPane().add(new JTable());
                        manager.addFrame(dockableFrame);
                        manager.showFrame(dockableFrame.getKey());
                    }
                }
            }
        });
        list.setToolTipText("This is a tooltip");
        frame.getContentPane().add(createScrollPane(list));
        frame.setPreferredSize(new Dimension(200, 200));
        frame.setMinimumSize(new Dimension(100, 100));
        return frame;
    }

    protected static DockableFrame createSampleCommandFrame() {
        DockableFrame frame = new DockableFrame("Command", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME7));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(1);
        JTextArea textArea = new JTextArea();
        frame.getContentPane().add(createScrollPane(textArea));
        textArea.setText(">");
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    protected static DockableFrame createSampleOutputFrame() {
        DockableFrame frame = new DockableFrame("Output", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME8));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(0);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    protected static DockableFrame createSampleFindResult1Frame() {
        DockableFrame frame = new DockableFrame("Find Results 1", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME9));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(0);
        frame.getContext().setCurrentDockSide(DockContext.DOCK_SIDE_SOUTH);
        JTextArea textArea = new JTextArea();
        frame.getContentPane().add(createScrollPane(textArea));
        textArea.setText("Find all \"TestDock\", Match case, Whole word, Find Results 1, All Open Documents\n" +
                "C:\\Projects\\src\\com\\jidesoft\\test\\TestDock.java(1):// TestDock.java : implementation of the TestDock class\n" +
                "C:\\Projects\\src\\jidesoft\\test\\TestDock.java(8):#import com.jidesoft.test.TestDock;\n" +
                "C:\\Projects\\src\\com\\jidesoft\\Test.java(10):#import com.jidesoft.test.TestDock;\n" +
                "Total found: 3    Matching files: 5    Total files searched: 5");
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    protected static DockableFrame createSampleFindResult2Frame() {
        DockableFrame frame = new DockableFrame("Find Results 2", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME10));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(1);
        JTextArea textArea = new JTextArea();
        frame.getContentPane().add(createScrollPane(textArea));
        textArea.setText("Find all \"TestDock\", Match case, Whole word, Find Results 2, All Open Documents\n" +
                "C:\\Projects\\src\\com\\jidesoft\\test\\TestDock.java(1):// TestDock.java : implementation of the TestDock class\n" +
                "C:\\Projects\\src\\jidesoft\\test\\TestDock.java(8):#import com.jidesoft.test.TestDock;\n" +
                "C:\\Projects\\src\\com\\jidesoft\\Test.java(10):#import com.jidesoft.test.TestDock;\n" +
                "Total found: 3    Matching files: 5    Total files searched: 5");
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    protected static DockableFrame createSampleFindResult3Frame() {
        DockableFrame frame = new DockableFrame("Find Results 3", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME11));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(1);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    protected static JMenuBar createMenuBar() {
        JMenuBar menu = new JMenuBar();

        JMenu fileMenu = createFileMenu();
        JMenu viewMenu = createViewMenu();
        JMenu windowMenu = createWindowsMenu();
        JMenu optionMenu = createOptionMenu();
        JMenu lnfMenu = createLnfMenu();
        JMenu helpMenu = createHelpMenu();

        menu.add(fileMenu);
        menu.add(viewMenu);
        menu.add(windowMenu);
        menu.add(optionMenu);
        menu.add(lnfMenu);
        menu.add(helpMenu);


        return menu;
    }

    private static JScrollPane createScrollPane(Component component) {
        JScrollPane pane = new JideScrollPane(component);
        return pane;
    }

    private static JMenu createHelpMenu() {
        JMenu menu = new JMenu("Help");
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
        JMenu menu = new JMenu("Window");
        menu.setMnemonic('W');

        JMenuItem item = null;

        _undoMenuItem = new JMenuItem("Undo");
        menu.add(_undoMenuItem);
        _redoMenuItem = new JMenuItem("Redo");
        menu.add(_redoMenuItem);
        _undoMenuItem.setEnabled(false);
        _redoMenuItem.setEnabled(false);

        _undoMenuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().undo();
                refreshUndoRedoMenuItems();
            }
        });
        _redoMenuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().redo();
                refreshUndoRedoMenuItems();
            }
        });

        menu.addSeparator();

        item = new JMenuItem("Load Default Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().loadLayoutData();
            }
        });
        menu.add(item);

        item = new JMenuItem("Load Design Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().loadLayoutDataFrom("design");
            }
        });
        menu.add(item);

        item = new JMenuItem("Load Debug Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().loadLayoutDataFrom("debug");
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Save as Default Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().saveLayoutData();
            }
        });
        menu.add(item);

        item = new JMenuItem("Save as Design Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().saveLayoutDataAs("design");
            }
        });
        menu.add(item);

        item = new JMenuItem("Save as Debug Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().saveLayoutDataAs("debug");
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Reset Layout");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().setUseFrameBounds(false);
                _frame.getDockingManager().setUseFrameState(false);
                _frame.getDockingManager().resetToDefault();
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Toggle Auto Hide All");
        item.setMnemonic('T');
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!_autohideAll) {
                    _frame.getDockingManager().saveLayoutDataAs("fullscreen");
                    _frame.getDockingManager().autohideAll();
                    _autohideAll = true;
                }
                else {
                    // call next two methods so that the farme bounds and state will not change.
                    _frame.getDockingManager().setUseFrameBounds(false);
                    _frame.getDockingManager().setUseFrameState(false);
                    _frame.getDockingManager().loadLayoutDataFrom("fullscreen");
                    _autohideAll = false;

                }
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Show Workspace Area");
        item.setMnemonic('S');
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showWorkspace();
            }
        });
        menu.add(item);

        item = new JMenuItem("Hide Workspace Area");
        item.setMnemonic('H');
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().hideWorkspace();
            }
        });
        menu.add(item);

        return menu;
    }

    private static void refreshUndoRedoMenuItems() {
        _undoMenuItem.setEnabled(_frame.getDockingManager().getUndoManager().canUndo());
        _undoMenuItem.setText(_frame.getDockingManager().getUndoManager().getUndoPresentationName());
        _redoMenuItem.setEnabled(_frame.getDockingManager().getUndoManager().canRedo());
        _redoMenuItem.setText(_frame.getDockingManager().getUndoManager().getRedoPresentationName());
    }

    private static JMenu createViewMenu() {
        JMenuItem item;
        JMenu menu = new JMenu("View");
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

        item = new JMenuItem("Task List", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME6));
        item.setMnemonic('T');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Task List");
            }
        });
        menu.add(item);

        item = new JMenuItem("Command Window", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME7));
        item.setMnemonic('N');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Command");
            }
        });
        menu.add(item);

        item = new JMenuItem("Output", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME8));
        item.setMnemonic('U');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Output");
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Find Results 1", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME9));
        item.setMnemonic('1');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Find Results 1");
            }
        });
        menu.add(item);

        item = new JMenuItem("Find Results 2", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME10));
        item.setMnemonic('3');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Find Results 2");
            }
        });
        menu.add(item);

        item = new JMenuItem("Find Results 3", JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME11));
        item.setMnemonic('3');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _frame.getDockingManager().showFrame("Find Results 3");
            }
        });
        menu.add(item);

        return menu;
    }

    private static JMenu createOptionMenu() {
        JMenu menu = new JMenu("Options");
        menu.setMnemonic('P');

        JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem("Frames Floatable");
        checkBoxMenuItem.setMnemonic('F');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    _frame.getDockingManager().setFloatable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isFloatable());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Autohidable");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    _frame.getDockingManager().setAutohidable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isAutohidable());

        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Hidable");
        checkBoxMenuItem.setMnemonic('H');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    _frame.getDockingManager().setHidable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isHidable());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Rearrangable");
        checkBoxMenuItem.setMnemonic('R');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    _frame.getDockingManager().setRearrangable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isHidable());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Frames Resizable");
        checkBoxMenuItem.setMnemonic('S');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem)
                    _frame.getDockingManager().setResizable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isResizable());
        menu.add(checkBoxMenuItem);

        menu.addSeparator();

        JMenu buttonsMenu = new JMenu("Available Titlebar Buttons");

        checkBoxMenuItem = new JCheckBoxMenuItem("Close Button");
        checkBoxMenuItem.setMnemonic('C');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(selected, DockableFrame.BUTTON_CLOSE);
            }
        });
        checkBoxMenuItem.setSelected(true);
        buttonsMenu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Autohide Button");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(selected, DockableFrame.BUTTON_AUTOHIDE);
            }
        });
        checkBoxMenuItem.setSelected(true);
        buttonsMenu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Float Button");
        checkBoxMenuItem.setMnemonic('F');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(selected, DockableFrame.BUTTON_FLOATING);
            }
        });
        checkBoxMenuItem.setSelected(true);
        buttonsMenu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Maximize Button");
        checkBoxMenuItem.setMnemonic('M');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                toggleButton(selected, DockableFrame.BUTTON_MAXIMIZE);
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
                    _frame.getDockingManager().setContinuousLayout(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (_frame.getDockingManager().isContinuousLayout()) {
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
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isContinuousLayout());
        menu.add(checkBoxMenuItem);

        menu.addSeparator();

        checkBoxMenuItem = new JCheckBoxMenuItem("Easy Tab Docking");
        checkBoxMenuItem.setMnemonic('E');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    _frame.getDockingManager().setEasyTabDock(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (_frame.getDockingManager().isEasyTabDock()) {
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
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isEasyTabDock());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Title Bar Dragging All Tabs");
        checkBoxMenuItem.setMnemonic('T');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    _frame.getDockingManager().setDragAllTabs(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (_frame.getDockingManager().isDragAllTabs()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<B><FONT COLOR='BLUE' FACE='Tahoma' SIZE='4'>Title Bar Dragging All Tabs</FONT></B>" +
                                "<BR><BR><FONT FACE='Tahoma' SIZE='3'><B>An option for title bar dragging</B>" +
                                "<BR><BR>If user drags the title bar, by default, all tabs in the tabbed pane will be dragged." +
                                "<BR>However if you set this options to be false, only the active tab will be dragged" +
                                "<BR>if user drags the title bar." +
                                "<BR><BR>Default: on</FONT>" +
                                "<BR></HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isDragAllTabs());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Allow Nested Floating Windows");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    _frame.getDockingManager().setNestedFloatingAllowed(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (_frame.getDockingManager().isNestedFloatingAllowed()) {
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
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isNestedFloatingAllowed());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Show Gripper");
        checkBoxMenuItem.setMnemonic('S');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    _frame.getDockingManager().setShowGripper(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (_frame.getDockingManager().isShowGripper()) {
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
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isShowGripper());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("Show TitleBar");
        checkBoxMenuItem.setMnemonic('T');
        checkBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    _frame.getDockingManager().getFrame("Property").setShowTitleBar(((JCheckBoxMenuItem) e.getSource()).isSelected());
//                    _frame.getDockingManager().setShowTitleBar(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (_frame.getDockingManager().isShowTitleBar()) {
                        Lm.showPopupMessageBox("<HTML>" +
                                "<FONT FACE='Tahoma' SIZE='4'><FONT COLOR='#0000FF'><B>Show TitleBar</B><BR></FONT><BR></FONT>" +
                                "<FONT FACE='Tahoma' SIZE='3'><B>An option to show/hide dockable frame's title bar<BR></B>" +
                                "<BR><BR>Default: on</FONT><BR>" +
                                "</HTML>");
                    }
                }
            }
        });
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isShowTitleBar());
        menu.add(checkBoxMenuItem);

        checkBoxMenuItem = new JCheckBoxMenuItem("SideBar Rollover");
        checkBoxMenuItem.setMnemonic('A');
        checkBoxMenuItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    _frame.getDockingManager().setSidebarRollover(((JCheckBoxMenuItem) e.getSource()).isSelected());
                    if (_frame.getDockingManager().isSidebarRollover()) {
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
        checkBoxMenuItem.setSelected(_frame.getDockingManager().isSidebarRollover());
        menu.add(checkBoxMenuItem);

        menu.addSeparator();

        JRadioButtonMenuItem radioButtonMenuItem1 = new JRadioButtonMenuItem("Draw Full Outline When Dragging");
        radioButtonMenuItem1.setMnemonic('D');
        radioButtonMenuItem1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JRadioButtonMenuItem) {
                    _frame.getDockingManager().setOutlineMode(DefaultDockingManager.FULL_OUTLINE_MODE);
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
        radioButtonMenuItem1.setSelected(_frame.getDockingManager().getOutlineMode() == DefaultDockingManager.FULL_OUTLINE_MODE);
        menu.add(radioButtonMenuItem1);

        JRadioButtonMenuItem radioButtonMenuItem2 = new JRadioButtonMenuItem("Draw Partial Outline When Dragging");
        radioButtonMenuItem2.setMnemonic('P');
        radioButtonMenuItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JRadioButtonMenuItem) {
                    _frame.getDockingManager().setOutlineMode(DefaultDockingManager.PARTIAL_OUTLINE_MODE);
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
        radioButtonMenuItem2.setSelected(_frame.getDockingManager().getOutlineMode() == DefaultDockingManager.PARTIAL_OUTLINE_MODE);
        menu.add(radioButtonMenuItem2);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButtonMenuItem1);
        buttonGroup.add(radioButtonMenuItem2);

        return menu;
    }

    private static void toggleButton(boolean selected, int button) {
        Collection names = _frame.getDockingManager().getAllFrames();
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            DockableFrame frame = _frame.getDockingManager().getFrame(name);
            if (selected) {
                frame.setAvailableButtons(frame.getAvailableButtons() | button);
            }
            else {
                frame.setAvailableButtons(frame.getAvailableButtons() & ~button);
            }
        }
    }

    private static JMenu createFileMenu() {
        JMenuItem item;

        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');

        item = new JMenuItem("Exit");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearUp();
//                System.exit(0);
            }
        });
        menu.add(item);
        return menu;
    }

    private static JMenu createLnfMenu() {
        JMenuItem item;

        JMenu lnfMenu = new JMenu("Look And Feel");
        lnfMenu.setMnemonic('L');

        item = new JMenuItem("Window Look And Feel (VSNET style)");
        item.setEnabled(SystemInfo.isWindows());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.WINDOWS_LNF);
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
                _frame.getDockingManager().updateComponentTreeUI();
                _frame.getRootPane().getJMenuBar().updateUI();
            }
        });
        lnfMenu.add(item);

        item = new JMenuItem("Windows Look And Feel (Office 2003 style)");
        item.setEnabled(SystemInfo.isWindows());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.WINDOWS_LNF);
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
                _frame.getDockingManager().updateComponentTreeUI();
                _frame.getRootPane().getJMenuBar().updateUI();
            }
        });
        lnfMenu.add(item);

        item = new JMenuItem("Windows Look And Feel (Eclipse style)");
        lnfMenu.add(item);
        item.setEnabled(SystemInfo.isWindows());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.WINDOWS_LNF);
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
                _frame.getDockingManager().updateComponentTreeUI();
                _frame.getRootPane().getJMenuBar().updateUI();
            }
        });

        lnfMenu.addSeparator();

        item = new JMenuItem("Metal Look And Feel (VSNET style)");
        lnfMenu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.METAL_LNF);
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
                _frame.getDockingManager().updateComponentTreeUI();
                _frame.getRootPane().getJMenuBar().updateUI();
            }
        });

        item = new JMenuItem("Metal Look And Feel (Eclipse style)");
        lnfMenu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.METAL_LNF);
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
                _frame.getDockingManager().updateComponentTreeUI();
                _frame.getRootPane().getJMenuBar().updateUI();
            }
        });

        lnfMenu.addSeparator();

        item = new JMenuItem("Aqua Look And Feel (Mac OS X)");
        lnfMenu.add(item);
        item.setEnabled(SystemInfo.isMacOSX());
        item.addActionListener(new ActionListener() {
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
                _frame.getDockingManager().updateComponentTreeUI();
                _frame.getRootPane().getJMenuBar().updateUI();
            }
        });

        // Shading effect is replaced by Office2003 L&F
        lnfMenu.addSeparator();

        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Add Shading Effect (only on VSNET style)");
        lnfMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (menuItem.isSelected()) {
                    System.setProperty("shadingtheme", "true");
                }
                else {
                    System.setProperty("shadingtheme", "false");
                }
                _frame.getDockingManager().updateComponentTreeUI();
                _frame.getRootPane().getJMenuBar().updateUI();
            }
        });

        return lnfMenu;
    }
}
