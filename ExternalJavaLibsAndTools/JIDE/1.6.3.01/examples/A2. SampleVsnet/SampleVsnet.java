/*
 * @(#)SampleVsnet.java
 *
 * Copyright 2002 - 2004 JIDE Software. All rights reserved.
 */

import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.jidesoft.action.ContentContainer;
import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.document.*;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.JideTabbedPaneUI;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.status.*;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.utils.Lm;
import com.jidesoft.utils.SystemInfo;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SampleVsnet extends DefaultDockableBarDockableHolder {

    private static SampleVsnet _frame;
    private static IDocumentPane _documentPane;

    private static StatusBar _statusBar;
    private static Timer _timer;
    private static final String PROFILE_NAME = "jidesoft-vsnetdemo";
    public static JMenuItem _redoMenuItem;
    public static JMenuItem _undoMenuItem;

    private static boolean _autohideAll = false;
    private static String _fullScreenLayout;

    public SampleVsnet(String title) throws HeadlessException {
        super(title);
    }

    public SampleVsnet() throws HeadlessException {
        this("");
    }

    public static void main(String[] args) {
        if (!SystemInfo.isJdk14Above()) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "JDK 1.4 or above is required for this demo.", "JIDE Software, Inc.", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        if (!SystemInfo.isJdk142Above()) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "JDK 1.4.2 or above is recommended for this demo for the best experience of seamless integration with Windows XP.", "JIDE Software, Inc.", JOptionPane.WARNING_MESSAGE);
        }

        if (SystemInfo.isMacOSX()) { // set special properties for Mac OS X
            System.setProperty("apple.laf.useScreenMenuBar", "true");
//            System.setProperty("apple.awt.brushMetalLook", "true");
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // to add attitional UIDefault for JIDE components
        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);

        _frame = new SampleVsnet("Demo of JIDE Action Framework and JIDE Docking Framework - Microsoft Visual Studio .NET");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());

        Lm.setParent(_frame);

        // add a widnow listener so that timer can be stopped when exit
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                clearUp();
                System.exit(0);
            }
        });

        // set the profile key
        _frame.getLayoutPersistence().setProfileKey(PROFILE_NAME);

// comment this if you don't want to use javax pref
//        _frame.getLayoutPersistence().setUsePref(false);

        // draw full outline when outside main JFrame
        _frame.getDockingManager().setOutlineMode(0);

// uncomment following to adjust the sliding speed of autohide frame
//        _frame.getDockingManager().setInitDelay(100);
//        _frame.getDockingManager().setSteps(1);
//        _frame.getDockingManager().setStepDelay(0);


        // create tabbed-document interface and add it to workspace area
        _documentPane = createDocumentTabs();
        _frame.getDockingManager().getWorkspace().setLayout(new BorderLayout());
        _frame.getDockingManager().getWorkspace().add((Component) _documentPane, BorderLayout.CENTER);

//        _frame.getDockingManager().setAutohidable(false);

// uncomment following lines if you want to customize the popup menu of DockableFrame
//       _frame.getDockingManager().setPopupMenuCustomizer(new com.jidesoft.docking.DockableBarPopupMenuCustomizer() {
//           public void customizePopupMenu(JPopupMenu menu, DockingManager dockingManager, DockableFrame dockableFrame, boolean onTab) {
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
//        _frame.setJMenuBar(createMenuBar());

        _frame.getDockableBarManager().setProfileKey(PROFILE_NAME);
        // add toolbar
        _frame.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createMenuCommandBar(_frame));
        _frame.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createStandardCommandBar());
        _frame.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createLayoutCommandBar());
        _frame.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createBuildCommandBar());
        _frame.getDockableBarManager().addDockableBar(VsnetCommandBarFactory.createFormattingCommandBar());

        // add status bar
        _statusBar = createStatusBar();
        _frame.getContentPane().add(_statusBar, BorderLayout.AFTER_LAST_LINE);

        _frame.getDockingManager().setUndoLimit(10);
        _frame.getDockingManager().beginLoadLayoutData();

//        _frame.getDockingManager().setFloatable(false);

        _frame.getDockingManager().setInitSplitPriority(DefaultDockingManager.SPLIT_SOUTH_NORTH_EAST_WEST);

        // add all dockable frames
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleTaskListFrame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleResourceViewFrame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleClassViewFrame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleProjectViewFrame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleServerFrame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSamplePropertyFrame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleFindResult1Frame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleFindResult2Frame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleOutputFrame());
        _frame.getDockingManager().addFrame(DockableFrameFactory.createSampleCommandFrame());

        _frame.getDockingManager().setDefaultFocusComponent((JComponent) _documentPane);

// just use default size. If you want to overwrite, you can call this method
//        _frame.getDockingManager().setInitBounds(new Rectangle(0, 0, 960, 800));

        // load layout information from previous session
        _frame.getLayoutPersistence().loadLayoutData();

        _frame.getDockingManager().setShowGripper(true);

//        _frame.getDockingManager().loadLayoutFrom(
//                _frame.getDockingManager().convertStream(
//                        _frame.getClass().getClassLoader().getResourceAsStream("resources/default.layout")));


// uncomment following line(s) if you want to limit certain feature.
// If you uncomment all four lines, then the dockable frame will not
// be moved anywhere.
//        _frame.getDockingManager().setRearrangable(false);
//        _frame.getDockingManager().setAutohidable(false);
//        _frame.getDockingManager().setFloatable(false);
//        _frame.getDockingManager().(setHidable(false);

//        _frame.setVisible(true);

        if (Lm.DEMO) {
            Lm.showDemoMessage();
        }

        _frame.toFront();
    }

    private static void clearUp() {
        if (_frame.getLayoutPersistence() != null) {
            _frame.getLayoutPersistence().saveLayoutData();
        }
        _documentPane = null;
        if (_statusBar != null && _statusBar.getParent() != null)
            _statusBar.getParent().remove(_statusBar);
        _statusBar = null;
        _frame.dispose();
        Lm.setParent(null);
    }

    private static StatusBar createStatusBar() {
        // setup status bar
        StatusBar statusBar = new StatusBar();
        final ProgressStatusBarItem progress = new ProgressStatusBarItem();
        progress.setCancelCallback(new ProgressStatusBarItem.CancelCallback() {
            public void cancelPerformed() {
                _timer.stop();
                _timer = null;
                progress.setStatus("Canceled");
                progress.showStatus();
            }
        });
        statusBar.add(progress, JideBoxLayout.VARY);
        ButtonStatusBarItem button = new ButtonStatusBarItem("READ-ONLY");
        button.setIcon(JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.BLANK));
        button.setPreferredWidth(20);
        statusBar.add(button, JideBoxLayout.FLEXIBLE);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_timer != null && _timer.isRunning())
                    return;
                _timer = new Timer(100, new ActionListener() {
                    int i = 0;

                    public void actionPerformed(ActionEvent e) {
                        if (i == 0)
                            progress.setProgressStatus("Initializing ......");
                        if (i == 10)
                            progress.setProgressStatus("Running ......");
                        if (i == 90)
                            progress.setProgressStatus("Completing ......");
                        progress.setProgress(i++);
                        if (i > 100)
                            _timer.stop();
                    }
                });
                _timer.start();
            }
        });

        final LabelStatusBarItem label = new LabelStatusBarItem("Line");
        label.setText("100:42");
        label.setPreferredWidth(60);
        label.setAlignment(JLabel.CENTER);
        statusBar.add(label, JideBoxLayout.FLEXIBLE);

        final OvrInsStatusBarItem ovr = new OvrInsStatusBarItem();
        ovr.setPreferredWidth(100);
        ovr.setAlignment(JLabel.CENTER);
        statusBar.add(ovr, JideBoxLayout.FLEXIBLE);

        final TimeStatusBarItem time = new TimeStatusBarItem();
        statusBar.add(time, JideBoxLayout.FLEXIBLE);
        final MemoryStatusBarItem gc = new MemoryStatusBarItem();
        gc.setPreferredWidth(100);
        statusBar.add(gc, JideBoxLayout.FLEXIBLE);

        return statusBar;
    }

    private static DocumentPane createDocumentTabs() {
        DocumentPane panel = new DocumentPane() {
            // add function to maximize (autohideAll) the document pane when mouse double clicks on the tabs of DocumentPane.
            protected IDocumentGroup createDocumentGroup() {
                IDocumentGroup group = super.createDocumentGroup();
                if (group instanceof JideTabbedPane) {
                    ((JideTabbedPaneUI) ((JideTabbedPane) group).getUI()).getTabPanel().addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                                if (!_autohideAll) {
                                    _fullScreenLayout = _frame.getDockingManager().getLayoutData();
                                    _frame.getDockingManager().autohideAll();
                                    _autohideAll = true;
                                }
                                else {
                                    // call next two methods so that the farme bounds and state will not change.
                                    _frame.getDockingManager().setUseFrameBounds(false);
                                    _frame.getDockingManager().setUseFrameState(false);
                                    if (_fullScreenLayout != null) {
                                        _frame.getDockingManager().setLayoutData(_fullScreenLayout);
                                    }
                                    _autohideAll = false;
                                }
                            }
                        }
                    });
                }
                return group;
            }
        };
        panel.setTabPlacement(javax.swing.JTabbedPane.TOP);
        JComponent editor = createTextArea("Readme.txt");
        final DocumentComponent document = new DocumentComponent(DockableFrameFactory.createScrollPane(editor),
                "Readme.txt", "Readme.txt",
                JideIconsFactory.getImageIcon(JideIconsFactory.FileType.TEXT));
        document.setDefaultFocusComponent(editor);
        document.addDocumentComponentListener(new DocumentComponentAdapter() {
            public void documentComponentClosing(DocumentComponentEvent e) {
                int ret = JOptionPane.showConfirmDialog(_frame, "<HTML><B>Do you want to save and close Readme.txt?" +
                        "<BR>&nbsp;&nbsp;&nbsp;Yes: close and save<BR>&nbsp;&nbsp;&nbsp;No: close but not save<BR>&nbsp;&nbsp;&nbsp;Cancel: cancel the closing process</B><BR><BR>" +
                        "This is just an example of how to add your own code to the document closing process. Nothing is saved.</HTML>", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    // save it
                    document.setAllowClosing(true);
                }
                else if (ret == JOptionPane.NO_OPTION) {
                    // don't save it
                    document.setAllowClosing(true);
                }
                else if (ret == JOptionPane.CANCEL_OPTION) {
                    // don't save it
                    document.setAllowClosing(false);
                }
            }
        });
        panel.openDocument(document);

        editor = createMultiViewDocument("EULA.htm");
        DocumentComponent document2 = new DocumentComponent(editor,
                "License", "License", JideIconsFactory.getImageIcon(JideIconsFactory.FileType.HTML));
        if (editor instanceof JideTabbedPane) {
            Component comp = ((JideTabbedPane) editor).getSelectedComponent();
            if (comp instanceof JScrollPane) {
                document2.setDefaultFocusComponent(((JScrollPane) comp).getViewport().getView());
            }
        }
        panel.openDocument(document2);

        editor = createTextArea("SampleVsnet.java");
        DocumentComponent document3 = new DocumentComponent(DockableFrameFactory.createScrollPane(editor),
                "SampleVsnet.java", "C:\\Program Files\\JideSoft\\Source Code\\SampleVsnet.java",
                JideIconsFactory.getImageIcon(JideIconsFactory.FileType.JAVA));
        document3.setDefaultFocusComponent(editor);
        panel.openDocument(document3);

// an example to use DockableBarPopupMenuCustomizer to customize popup menu for DocumentPane
//        panel.setPopupMenuCustomizer(new DockableBarPopupMenuCustomizer(){
//            public void customizePopupMenu(JPopupMenu menu, final IDocumentPane pane, final String dragComponentName, final IDocumentGroup dropGroup, boolean onTab) {
//                menu.remove(0);
//                menu.insert(new AbstractAction("Say Hello") {
//                    public void actionPerformed(ActionEvent e) {
//                        JOptionPane.showMessageDialog(null, "Hello");
//                    }
//                }, 0);
//            }
//        });

        return panel;
    }

    private static JComponent createMultiViewDocument(String fileName) {
        JideTabbedPane pane = new JideTabbedPane(JideTabbedPane.BOTTOM);
        pane.setBoxStyleTab(true);
        pane.addTab("Design", JideIconsFactory.getImageIcon(JideIconsFactory.View.DESIGN), DockableFrameFactory.createScrollPane(createHtmlArea(fileName)));
        pane.addTab("HTML", JideIconsFactory.getImageIcon(JideIconsFactory.View.HTML), DockableFrameFactory.createScrollPane(createTextArea(fileName)));
        return pane;
    }

    public static JComponent createTextArea(String fileName) {
        JTextArea area = new JTextArea();
        Document doc = new PlainDocument();
        try {
            InputStream in = SampleVsnet.class.getResourceAsStream(fileName);
            if (in == null) {
                in = new FileInputStream(fileName);
            }
            byte[] buff = new byte[4096];
            int nch;
            while ((nch = in.read(buff, 0, buff.length)) != -1) {
                doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
            }
            area.setDocument(doc);
        }
        catch (FileNotFoundException e) {
            System.out.println(e.getLocalizedMessage());
        }
        catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        catch (BadLocationException e) {
            System.out.println(e.getLocalizedMessage());
        }

        return area;
    }

    public static Component createHtmlArea(String fileName) {
        JEditorPane area = new JEditorPane();
        try {
            URL url = SampleVsnet.class.getResource(fileName);
            if (url != null) {
                area.setPage(url);
            }
            else {
                area.setPage("file://" + fileName);
            }
        }
        catch (IOException e) {
        }
        return area;
    }

    protected ContentContainer createContentContainer() {
        return new LogoContentContainer();
    }

    class LogoContentContainer extends ContentContainer {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ImageIcon imageIcon = JideIconsFactory.getImageIcon(JideIconsFactory.JIDELOGO_SMALL);
            imageIcon.paintIcon(this, g, getWidth() - imageIcon.getIconWidth() - 2, 2);
        }
    }
}
