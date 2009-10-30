/*
 * @(#)JideSplitPaneDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Demoed Component: {@link JideSplitPane}
 * <br>
 * Required jar files: jide-common.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class JideSplitPaneDemo extends JFrame {

    private static JideSplitPaneDemo _frame;
    private static JideSplitPane _splitPane;

    public JideSplitPaneDemo(String title) throws HeadlessException {
        super(title);
    }

    public JideSplitPaneDemo() throws HeadlessException {
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
        _frame = new JideSplitPaneDemo("Demo of JideSplitPane - multiple split is allowed");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _splitPane = createSplitPane();
        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(_splitPane, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 800, 500);

        _frame.setVisible(true);

    }

    private static JideSplitPane createSplitPane() {
        JTree tree1 = new JTree();
        JTable table = new JTable(new DefaultTableModel() {
            public int getColumnCount() {
                return 3;
            }
        });
        JTree tree2 = new JTree();

        JideSplitPane split = new JideSplitPane(JideSplitPane.HORIZONTAL_SPLIT);
        split.add(new JScrollPane(tree1), JideBoxLayout.FLEXIBLE);
        split.add(new JScrollPane(table), JideBoxLayout.VARY);
        split.add(new JScrollPane(tree2), JideBoxLayout.FLEXIBLE);
        return split;
    }
}
