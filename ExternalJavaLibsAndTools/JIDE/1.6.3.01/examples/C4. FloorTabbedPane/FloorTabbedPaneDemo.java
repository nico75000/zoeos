/*
 * @(#)FloorTabbedPaneDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.pane.FloorTabbedPane;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideButton;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * Demoed Component: {@link FloorTabbedPane}
 * <br>
 * Required jar files: jide-common.jar, jide-components.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class FloorTabbedPaneDemo extends JFrame {

    private static FloorTabbedPaneDemo _frame;
    private static FloorTabbedPane _tabbedPane;

    public FloorTabbedPaneDemo(String title) throws HeadlessException {
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
        LookAndFeelFactory.installJideExtension();

        _frame = new FloorTabbedPaneDemo("Demo of FloorTabbedPane");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _tabbedPane = createTabbedPane();

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(_tabbedPane, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 200, 500);

        _frame.setVisible(true);

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

    // an example of creating your own button for floor tabbed pane.
    private static class FloorButton extends JideButton implements UIResource {
        public FloorButton(Action a) {
            super(a);
            setButtonStyle(TOOLBOX_STYLE);
            setOpaque(true); // you can try to set to false to see the difference.
        }
    }

    private static FloorTabbedPane createTabbedPane() {
        FloorTabbedPane tabbedPane = new FloorTabbedPane() {
            protected AbstractButton createButton(Action action) {
                return new FloorButton(action);
            }
        };
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
