/*
 * @(#)LargeSortableTableDemo.java
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.comparator.ObjectComparatorManager;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.SortableTableModel;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JidePopupMenu;
import com.jidesoft.swing.MultilineLabel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * Demoed Component: {@link SortableTable}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: any L&F
 */
public class LargeSortableTableDemo extends JPanel {

    private JTabbedPane _tabbedPane;
    private JScrollPane _tableScrollPane;
    private JScrollPane _sortableTableScrollPane;

    private static JFrame _frame;
    private static DefaultTableModel _model;
    private SortableTable _sortableTable;

    public LargeSortableTableDemo() {
        LookAndFeelFactory.installJideExtension();
        initComponents();

        _model = new LargeTableModel();
        _model.addColumn("int");
        _model.addColumn("double");
        _model.addColumn("boolean");
        _model.addColumn("string");
        for (int i = 0; i < 10000; i++) {
            _model.addRow(new Object[]{new Integer(i * 1024), new Double(Math.random()), new Boolean(i % 2 == 0), new String("row" + i)});
        }

        _sortableTable = new SortableTable(_model);
        _sortableTable.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int column = ((JTableHeader) e.getSource()).getColumnModel().getColumnIndexAtX(e.getPoint().x);
                JMenuItem[] menuItems = ((SortableTableModel) _sortableTable.getModel()).getPopupMenuItems(column);
                JPopupMenu popupMenu = new JidePopupMenu();
                for (int i = 0; i < menuItems.length; i++) {
                    JMenuItem item = menuItems[i];
                    popupMenu.add(item);
                }
                popupMenu.show((Component) e.getSource(), e.getPoint().x, e.getPoint().y);
            }
        });
        _sortableTableScrollPane.setViewportView(_sortableTable);

        JTable normalTable = new JTable(_model);
        _tableScrollPane.setViewportView(normalTable);
    }

    public static class LargeTableModel extends DefaultTableModel {

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
    }

    private void initComponents() {
        _tabbedPane = new JTabbedPane();
        _sortableTableScrollPane = new JScrollPane();
        _tableScrollPane = new JScrollPane();

        setLayout(new BorderLayout());

        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.addButton(new JButton(new AbstractAction("Insert a row") {
            public void actionPerformed(ActionEvent e) {
                int row = _sortableTable.getSelectedRow();
                if (row == -1) {
                    row = _sortableTable.getRowCount();
                }
                _model.insertRow(row, new Object[]{new Integer(100000), new Double(Math.random()), Boolean.FALSE, new String("new row")});
                int visualRow = _sortableTable.getSortedRowAt(row);
                _sortableTable.changeSelection(visualRow, 0, false, false);
            }
        }));
        buttonPanel.addButton(new JButton(new AbstractAction("Delete selected rows") {
            public void actionPerformed(ActionEvent e) {
                int[] rows = _sortableTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    int row = rows[i];
                    rows[i] = _sortableTable.getActualRowAt(row);
                }
                Arrays.sort(rows);
                for (int i = rows.length - 1; i >= 0; i--) {
                    int row = rows[i];
                    _model.removeRow(row);
                }
            }
        }));

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(buttonPanel, BorderLayout.BEFORE_FIRST_LINE);
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
        LargeSortableTableDemo demo = new LargeSortableTableDemo();
        _frame = new JFrame("LargeSortableTable Sample");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(demo, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 400, 500);
        _frame.setVisible(true);
    }
}
