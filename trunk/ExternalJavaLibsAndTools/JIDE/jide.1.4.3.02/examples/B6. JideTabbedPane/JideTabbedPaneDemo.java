/*
 * @(#)JideTabbedPaneDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.eclipse.EclipseWindowsLookAndFeel;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Demoed Component: JideTabbedPane
 * <br>
 * Required jar files: jide-common.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class JideTabbedPaneDemo extends JFrame {

    private static JideTabbedPaneDemo _frame;
    private static JideTabbedPane _tabbedPane;

    public JideTabbedPaneDemo(String title) throws HeadlessException {
        super(title);
    }

    public JideTabbedPaneDemo() throws HeadlessException {
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

        _frame = new JideTabbedPaneDemo("Demo of JideTabbedPane");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _tabbedPane = createTabbedPane();

        JideSplitPane pane = new JideSplitPane();
        pane.add(_tabbedPane, JideBoxLayout.VARY);
        pane.add(_frame.createOptionsPanel(), JideBoxLayout.FLEXIBLE);
        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(pane, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 800, 500);

        _frame.setVisible(true);

    }

    private Component createOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Options");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(new Color(0, 0, 128));
        header.setForeground(Color.WHITE);
        header.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.lightGray, Color.gray),
                        BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        header.setPreferredSize(new Dimension(100, 30));
        panel.add(header, JideBoxLayout.FLEXIBLE);

        panel.add(Box.createVerticalStrut(20), JideBoxLayout.FIX);
        final ButtonGroup group = new ButtonGroup();
        final JRadioButton top = new JRadioButton("Tabs on Top");
        final JRadioButton bottom = new JRadioButton("Tabs on Bottom");
        group.add(top);
        group.add(bottom);
        bottom.setSelected(true);
        top.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (top.isSelected()) {
                    _tabbedPane.setTabPlacement(JideTabbedPane.TOP);
                } else {
                    _tabbedPane.setTabPlacement(JideTabbedPane.BOTTOM);
                }
            }
        });
        panel.add(top);
        panel.add(bottom);
        panel.add(Box.createVerticalStrut(20), JideBoxLayout.FIX);
        final JCheckBox show = new JCheckBox("Always Show Buttons");
        show.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setShowTabButtons(show.isSelected());
            }
        });
        panel.add(show);
        final JCheckBox hide = new JCheckBox("Hidden Tab Area if Only One Tab");
        hide.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setHideOneTab(hide.isSelected());
            }
        });
        panel.add(hide);
        final JCheckBox fit = new JCheckBox("Shrink Tabs to Fit in");
        fit.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setShrinkTabs(fit.isSelected());
            }
        });
        panel.add(fit);
        final JCheckBox box = new JCheckBox("Box Style Tabs");
        box.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setBoxStyleTab(box.isSelected());
            }
        });
        panel.add(box);

        final JCheckBox useDefaultIcon = new JCheckBox("Use Default Value of Show Icons");
        panel.add(useDefaultIcon);

        final JCheckBox icon = new JCheckBox("Show Icons");
        icon.setSelected(_tabbedPane.isShowIconsOnTab());
        icon.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setShowIconsOnTab(icon.isSelected());
            }
        });
        panel.add(icon);

        useDefaultIcon.setSelected(_tabbedPane.isUseDefaultShowIconsOnTab());
        icon.setEnabled(!useDefaultIcon.isSelected());
        useDefaultIcon.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setUseDefaultShowIconsOnTab(useDefaultIcon.isSelected());
                icon.setEnabled(!useDefaultIcon.isSelected());
            }
        });

        final JCheckBox useDefaultClose = new JCheckBox("Use Default Value of Show Close Button On Tab");
        panel.add(useDefaultClose);

        final JCheckBox close = new JCheckBox("Show Close Button on Tab");
        close.setSelected(_tabbedPane.isShowCloseButtonOnTab());
        close.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setShowCloseButtonOnTab(close.isSelected());
            }
        });
        panel.add(close);

        useDefaultClose.setSelected(_tabbedPane.isUseDefaultShowCloseButtonOnTab());
        close.setEnabled(!useDefaultClose.isSelected());
        useDefaultClose.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setUseDefaultShowCloseButtonOnTab(useDefaultClose.isSelected());
                close.setEnabled(!useDefaultClose.isSelected());
            }
        });

        final JCheckBox editing = new JCheckBox("Allow Editing Tab Title");
        editing.setSelected(_tabbedPane.isTabEditingAllowed());
        editing.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _tabbedPane.setTabEditingAllowed(editing.isSelected());
            }
        });
        panel.add(editing);

        panel.add(Box.createGlue(), JideBoxLayout.VARY);
        return panel;
    }

    private static class TabPanel extends JPanel {
        Icon _icon;
        String _title;
        JComponent _component;

        public TabPanel(String title, Icon icon, JComponent component) {
            _title = title;
            _icon = icon;
            _component = component;
        }

        public Icon getIcon() {
            return _icon;
        }

        public void setIcon(Icon icon) {
            _icon = icon;
        }

        public String getTitle() {
            return _title;
        }

        public void setTitle(String title) {
            _title = title;
        }

        public JComponent getComponent() {
            return _component;
        }

        public void setComponent(JComponent component) {
            _component = component;
        }
    }

    private static JideTabbedPane createTabbedPane() {
        JideTabbedPane tabbedPane = new JideTabbedPane(JideTabbedPane.BOTTOM);
        TabPanel frame = createTabPanel(1);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        frame = createTabPanel(2);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        frame = createTabPanel(3);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        frame = createTabPanel(4);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        frame = createTabPanel(5);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        frame = createTabPanel(6);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        frame = createTabPanel(7);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        frame = createTabPanel(8);
        tabbedPane.addTab(frame.getTitle(), frame.getIcon(), frame.getComponent());
        return tabbedPane;
    }

    protected static TabPanel createTabPanel(int index) {
        TabPanel frame = new TabPanel("Tab " + index,
                JideIconsFactory.getImageIcon("jide/dockableframe_" + index + ".gif"),
                new JScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }
}
