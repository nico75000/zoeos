/*
 * @(#)SortableTableDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.comparator.ObjectComparatorManager;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.MultilineLabel;
import com.jidesoft.plaf.LookAndFeelFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Demoed Component: {@link SortableTable}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: any L&F
 */
public class SortableTableDemo extends JPanel {

    private JTabbedPane _tabbedPane;
    private JScrollPane _tableScrollPane;
    private JScrollPane _sortableTableScrollPane;

    private static JFrame _frame;

    public SortableTableDemo() {
        initComponents();

        TableModel model = new SampleTableModel();

        final SortableTable sortableTable = new SortableTable(model);
        sortableTable.setColumnSelectionAllowed(true);
        sortableTable.setRowSelectionAllowed(true);
        _sortableTableScrollPane.setViewportView(sortableTable);

        JTable normalTable = new JTable(model);
        _tableScrollPane.setViewportView(normalTable);
    }

    public static class SampleTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return 4;
        }

        public int getRowCount() {
            return 8;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "int column";
                case 1:
                    return "double column";
                case 2:
                    return "boolean column";
                case 3:
                    return "string column";
            }
            return "";
        }

        public Class getColumnClass(int column) {
            switch (column) {
                case 0:
                    return Integer.class;
                case 1:
                    return Double.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return String.class;
            }
            return Object.class;
        }

        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    if (row > 4) {
                        return new Integer(2);
                    } else {
                        return new Integer(row);
                    }
                case 1:
                    return new Double(row);
                case 2:
                    if (row % 2 == 0)
                        return Boolean.TRUE;
                    return Boolean.FALSE;
                case 3:
                    return new String("row " + (getRowCount() - row));
            }
            return null;
        }
    }

    private void initComponents() {
        _tabbedPane = new JTabbedPane();
        _sortableTableScrollPane = new JScrollPane();
        _tableScrollPane = new JScrollPane();

        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(_sortableTableScrollPane, BorderLayout.CENTER);
        panel.add(new MultilineLabel("Click once on the header to sort ascending, click twice to sort descending, a third time to unsort. " +
                "\nHold CTRL key then click on several headers to see mulitple columns sorting."), BorderLayout.AFTER_LAST_LINE);
        _tabbedPane.addTab("SortableTable", panel);

        _tabbedPane.addTab("JTable", _tableScrollPane);

        add(_tabbedPane, BorderLayout.CENTER);

    }


    static public void main(String[] s) {
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

        ObjectComparatorManager.initDefaultComparator();
        SortableTableDemo demo = new SortableTableDemo();
        _frame = new JFrame("SortableTable Sample");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(demo, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 400, 500);

        _frame.setVisible(true);
    }
}
