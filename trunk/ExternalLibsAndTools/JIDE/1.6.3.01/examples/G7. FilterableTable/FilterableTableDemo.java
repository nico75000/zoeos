/*
 * @(#)FilterableTableDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.comparator.ObjectComparatorManager;
import com.jidesoft.grid.*;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.MultilineLabel;
import com.jidesoft.swing.JidePopupMenu;
import com.jidesoft.plaf.LookAndFeelFactory;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * Demoed Component: {@link SortableTable}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: any L&F
 */
public class FilterableTableDemo extends JPanel {

    private JTabbedPane _tabbedPane;
    private JScrollPane _tableScrollPane;
    private JScrollPane _sortableTableScrollPane;

    private static JFrame _frame;
    public JCheckBox _booleanFilterCheckBox;
    public JCheckBox _intFilterCheckBox;
    private FilterableTableModel _filterableTableModel;
    private AbstractFilter _booleanFilter;
    private AbstractFilter _intFilter;

    public FilterableTableDemo() {
        initFilters();
        initComponents();

        TableModel model = new SampleTableModel();

        _filterableTableModel = new FilterableTableModel(model);
        _filterableTableModel.addFilter(0, _intFilter);
        _filterableTableModel.addFilter(2, _booleanFilter);
        _filterableTableModel.setFiltersApplied(true);

        final SortableTable sortableTable = new SortableTable(_filterableTableModel);
        sortableTable.setColumnSelectionAllowed(true);
        sortableTable.setRowSelectionAllowed(true);

        sortableTable.getTableHeader().addMouseListener(new MouseAdapter() {
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
                JMenuItem[] menuItems = _filterableTableModel.getPopupMenuItems(column);
                JPopupMenu popupMenu = new JidePopupMenu();
                popupMenu.setRequestFocusEnabled(false);
                for (int i = 0; i < menuItems.length; i++) {
                    JMenuItem item = menuItems[i];
                    popupMenu.add(item);
                }
                popupMenu.show((Component) e.getSource(), e.getPoint().x, e.getPoint().y);
            }
        });

        _sortableTableScrollPane.setViewportView(sortableTable);

        JTable normalTable = new JTable(_filterableTableModel);
        _tableScrollPane.setViewportView(normalTable);
    }

    private void initFilters() {
        _intFilter = new AbstractFilter("\"int column\" value == 2") {
            public boolean isValueFiltered(Object value) {
                if (value instanceof Integer) {
                    if (((Integer) value).intValue() == 2)
                        return false;
                }
                return true;
            }
        };

        _booleanFilter = new AbstractFilter("\"boolean column\" value == true") {
            public boolean isValueFiltered(Object value) {
                if (Boolean.TRUE.equals(value))
                    return false;
                else
                    return true;
            }
        };
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
        _tabbedPane = new JTabbedPane();
        _sortableTableScrollPane = new JScrollPane();
        _tableScrollPane = new JScrollPane();

        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel checkBoxPanel = new JPanel(new GridLayout(2, 1));
        _booleanFilterCheckBox = new JCheckBox("Filter " + _booleanFilter.getName());
        _booleanFilterCheckBox.setSelected(true);
        _booleanFilterCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                _booleanFilter.setEnabled(_booleanFilterCheckBox.isSelected());
                _filterableTableModel.refresh();
            }
        });
        _booleanFilter.addFilterListener(new FilterListener() {
            public void filterChanged(FilterEvent event) {
                if(event.getID() == FilterEvent.FILTER_ENABLED) {
                    _booleanFilterCheckBox.setSelected(true);
                }
                else if(event.getID() == FilterEvent.FILTER_DISABLED) {
                    _booleanFilterCheckBox.setSelected(false);
                }
            }
        });
        _intFilterCheckBox = new JCheckBox("Filter " + _intFilter.getName());
        _intFilterCheckBox.setSelected(true);
        _intFilterCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                _intFilter.setEnabled(_intFilterCheckBox.isSelected());
                _filterableTableModel.refresh();
            }
        });
        _intFilter.addFilterListener(new FilterListener() {
            public void filterChanged(FilterEvent event) {
                if(event.getID() == FilterEvent.FILTER_ENABLED) {
                    _intFilterCheckBox.setSelected(true);
                }
                else if(event.getID() == FilterEvent.FILTER_DISABLED) {
                    _intFilterCheckBox.setSelected(false);
                }
            }
        });
        checkBoxPanel.add(_intFilterCheckBox);
        checkBoxPanel.add(_booleanFilterCheckBox);

        panel.add(_sortableTableScrollPane, BorderLayout.CENTER);
        panel.add(new MultilineLabel("Click once on the header to sort ascending, click twice to sort descending, a third time to unsort. " +
                "\nHold CTRL key then click on several headers to see mulitple columns sorting."), BorderLayout.AFTER_LAST_LINE);
        _tabbedPane.addTab("SortableTable", panel);

        _tabbedPane.addTab("JTable", _tableScrollPane);

        add(checkBoxPanel, BorderLayout.BEFORE_FIRST_LINE);
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
        FilterableTableDemo demo = new FilterableTableDemo();
        _frame = new JFrame("FilterableTableModel in SortableTable Sample");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(demo, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 400, 500);

        _frame.setVisible(true);
    }
}
