/*
 * @(#)TipsOfTheDayDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.tipoftheday.ResourceBundleTipOfTheDaySource;
import com.jidesoft.tipoftheday.TipOfTheDayDialog;
import com.jidesoft.utils.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Demoed Component: {@link TipOfTheDayDialog}
 * <br>
 * Required jar files: jide-common.jar, jide-dialogs.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class TipsOfTheDayDemo extends JFrame {

    private static TipsOfTheDayDemo _frame;

    private static final String PROFILE_NAME = "jidesoft";

    public TipsOfTheDayDemo(String title) throws HeadlessException {
        super(title);
    }

    public TipsOfTheDayDemo() throws HeadlessException {
        this("");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LookAndFeelFactory.installJideExtension();
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (UnsupportedLookAndFeelException e) {
        }

        _frame = new TipsOfTheDayDemo("Demo of Tips of the Day");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // add menu bar
        _frame.setJMenuBar(createMenuBar());
        JTextArea textArea = new JTextArea();
        textArea.setPreferredSize(new Dimension(800, 600));
        _frame.getContentPane().add(new JScrollPane(textArea));
        _frame.pack();
        _frame.show();

        boolean showTip = getPrefBooleanValue("tip", true);
        if (showTip) {
            showTipsOfTheDay();
        }
    }

    private static boolean getPrefBooleanValue(String key, boolean defaultValue) {
        Preferences prefs = Preferences.userRoot();
        return prefs.node(PROFILE_NAME).getBoolean(key, defaultValue);
    }

    private static void setPrefBooleanValue(String key, boolean value) {
        Preferences prefs = Preferences.userRoot();
        prefs.node(PROFILE_NAME).putBoolean(key, value);
    }

    protected static JMenuBar createMenuBar() {
        JMenuBar menu = new JMenuBar();

        JMenu fileMenu = createFileMenu();
        JMenu lnfMenu = createLnfMenu();
        JMenu helpMenu = createHelpMenu();

        menu.add(fileMenu);
        menu.add(lnfMenu);
        menu.add(helpMenu);


        return menu;
    }

    private static JMenu createHelpMenu() {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic('H');

        JMenuItem item = new JMenuItem("Tips of the Day");
        item.setMnemonic('T');
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showTipsOfTheDay();
            }
        });
        menu.add(item);

        return menu;
    }

    private static void showTipsOfTheDay() {
        ResourceBundleTipOfTheDaySource tipOfTheDaySource = new ResourceBundleTipOfTheDaySource(ResourceBundle.getBundle("tips"));
        tipOfTheDaySource.setCurrentTipIndex(-1);
        URL styleSheet = TipOfTheDayDialog.class.getResource("/tips.css");
        TipOfTheDayDialog dialog = new TipOfTheDayDialog(_frame, tipOfTheDaySource, new AbstractAction("Show Tips on startup") {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) e.getSource();
                    setPrefBooleanValue("tip", checkBox.isSelected());
                }
                // change your user preference
            }
        }, styleSheet);

        dialog.setShowTooltip(getPrefBooleanValue("tip", true));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocation(200, 200);
        dialog.show();
    }

    private static JMenu createFileMenu() {
        JMenuItem item;

        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');

        item = new JMenuItem("Exit");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(item);
        return menu;
    }

    private static JMenu createLnfMenu() {
        JMenuItem item;

        JMenu menu = new JMenu("Look And Feel");
        menu.setMnemonic('L');

        item = new JMenuItem("Window Look And Feel");
        item.setEnabled(SystemInfo.isWindows());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.WINDOWS_LNF));
                    SwingUtilities.updateComponentTreeUI(_frame);
                } catch (UnsupportedLookAndFeelException e1) {
                    e1.printStackTrace();
                }
            }
        });
        menu.add(item);

        item = new JMenuItem("Metal Look And Feel");
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.METAL_LNF));
                    SwingUtilities.updateComponentTreeUI(_frame);
                } catch (UnsupportedLookAndFeelException e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new JMenuItem("Aqua Look And Feel (Mac OS X)");
        menu.add(item);
        item.setEnabled(SystemInfo.isMacOSX());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.AQUA_LNF));
                    SwingUtilities.updateComponentTreeUI(_frame);
                } catch (UnsupportedLookAndFeelException e1) {
                    e1.printStackTrace();
                }

            }
        });

        return menu;
    }
}
