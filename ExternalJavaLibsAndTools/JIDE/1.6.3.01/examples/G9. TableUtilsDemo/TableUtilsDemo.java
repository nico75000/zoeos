/*
 * @(#)${NAME}
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.comparator.ObjectComparatorManager;
import com.jidesoft.grid.TableUtils;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.utils.Lm;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Demoed Component: {@link com.jidesoft.grid.TableUtils}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: any L&F
 */
public class TableUtilsDemo extends JPanel {
    private static JFrame _frame;
    final JTable _table;
    private TableModel _model;

    private int[] _selections;
    private JButton _saveSelectionButton;
    private JButton _loadSelectionButton;

    private String _tablePref;
    private JButton _savePrefButton;
    private JButton _loadPrefButton;


    public TableUtilsDemo() {
        _model = new SampleTableModel();
        _table = new JTable(_model);
        _table.setRowSelectionAllowed(true);
        _table.setColumnSelectionAllowed(true);
        initComponents();
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
                    return "1: int column";
                case 1:
                    return "2: double column";
                case 2:
                    return "3: boolean column";
                case 3:
                    return "4: string column";
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
                    }
                    else {
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
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel control = new JPanel(new GridLayout(2, 2));
        _saveSelectionButton = new JButton(new AbstractAction("Save Selection") {
            public void actionPerformed(ActionEvent e) {
                _selections = TableUtils.saveSelection(_table);
                Lm.showPopupMessageBox("<HTML>The selection has been saved. You can clear the selection in the table now. " +
                        "<BR>After you clear it, press \"Load Selection\" to restore the saved selection.</HTML>");
            }
        });
        _loadSelectionButton = new JButton(new AbstractAction("Load Selection") {
            public void actionPerformed(ActionEvent e) {
                TableUtils.loadSelection(_table, _selections);
            }
        });
        _savePrefButton = new JButton(new AbstractAction("Save Preference") {
            public void actionPerformed(ActionEvent e) {
                _tablePref = TableUtils.getTablePreference(_table);
                Lm.showPopupMessageBox("<HTML>The table column width and column order information has been saved. You can change width and order in the table now. " +
                        "<BR>After you are done with it, press \"Load Preference\" to restore the saved column width and order.</HTML>");
            }
        });
        _loadPrefButton = new JButton(new AbstractAction("Load Preference") {
            public void actionPerformed(ActionEvent e) {
                TableUtils.setTablePreference(_table, _tablePref);
            }
        });
        control.add(_saveSelectionButton);
        control.add(_loadSelectionButton);
        control.add(_savePrefButton);
        control.add(_loadPrefButton);
        panel.add(_table, BorderLayout.CENTER);
        add(control, BorderLayout.BEFORE_FIRST_LINE);
        add(new JScrollPane(_table), BorderLayout.CENTER);
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
        TableUtilsDemo demo = new TableUtilsDemo();
        _frame = new JFrame("TableUtils Demo");
        Lm.setParent(_frame);
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(demo, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 400, 500);

        _frame.setVisible(true);
    }
}
