/*
 * @(#)PropertyPaneDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.grid.*;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.utils.Lm;
import com.jidesoft.swing.JideSwingUtilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/**
 * Demoed Component: {@link PropertyPane}, {@link PropertyTable}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class HierarchicalTableDemo extends JFrame {

    protected static final Color BG1 = new Color(232, 237, 230);
    protected static final Color BG2 = new Color(243, 234, 217);
    protected static final Color BG3 = new Color(214, 231, 247);

    private static HierarchicalTableDemo _frame;
    private static HierarchicalTable _table;
    protected static TableModel _productsTableModel;

    public HierarchicalTableDemo(String title) throws HeadlessException {
        super(title);
    }

    public HierarchicalTableDemo() throws HeadlessException {
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

        ObjectConverterManager.initDefaultConverter();
        CellEditorManager.initDefaultEditor();
        CellRendererManager.initDefaultRenderer();

        _frame = new HierarchicalTableDemo("HierarchicalTable Sample");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                _frame.dispose();
                System.exit(0);
            }
        });

        _table = createTable();

        ButtonPanel buttonPanel = new ButtonPanel();
        JButton button = new JButton(new AbstractAction("Insert a row") {
            public void actionPerformed(ActionEvent e) {
                ((DefaultTableModel) _productsTableModel).insertRow(0, new Object[]{"JIDE Action Framework", "Feature drag-n-dropable toolbar/menu bar components"});
            }
        });
        button.setRequestFocusEnabled(false);
        buttonPanel.addButton(button);

        button = new JButton(new AbstractAction("Delete selected rows") {
            public void actionPerformed(ActionEvent e) {
                int[] rows = _table.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    int row = rows[i];
                    rows[i] = TableModelWrapperUtils.getActualRowAt(_table.getModel(), row, HierarchicalTableModel.class);
                }
                Arrays.sort(rows);
                for (int i = rows.length - 1; i >= 0; i--) {
                    int row = rows[i];
                    ((DefaultTableModel) _productsTableModel).removeRow(row);
                }
            }
        });
        button.setRequestFocusEnabled(false);
        buttonPanel.addButton(button);

        button = new JButton(new AbstractAction("Update a cell") {
            public void actionPerformed(ActionEvent e) {
                _productsTableModel.setValueAt("Collection of useful components include tabbed document interface, status bar, floor tabbed pane and collapsible pane.", 1, 1);
            }
        });
        button.setRequestFocusEnabled(false);
        buttonPanel.addButton(button);

//        button = new JButton(new AbstractAction("RTL/LTR") {
//            public void actionPerformed(ActionEvent e) {
//                JideSwingUtilities.toggleRTLnLTR(_frame);
//            }
//        });
//        button.setRequestFocusEnabled(false);
//        buttonPanel.addButton(button);

        _frame.getContentPane().setLayout(new BorderLayout(6, 6));
        _frame.getContentPane().add(buttonPanel, BorderLayout.BEFORE_FIRST_LINE);
        _frame.getContentPane().add(new JScrollPane(_table), BorderLayout.CENTER);

        _frame.setBounds(10, 10, 600, 500);

        _frame.setVisible(true);
    }

    // create property table
    private static HierarchicalTable createTable() {
        _productsTableModel = new ProductTableModel();
        HierarchicalTable table = new HierarchicalTable();
        table.setModel(_productsTableModel);
        table.setBackground(BG1);
//        table.setGridColor(Color.YELLOW);
        table.setName("Product Table");
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }


    static String[] DETAIL_COLUMNS = new String[]{"Feature", "Description"};
    static String[] PRODUCT_COLUMNS = new String[]{"Name", "Description"};

    static String[][] JIDE_PRODUCTS = new String[][]{
        new String[]{"JIDE Docking Framework", "Swing-based GUI Framework which enables drag-n-drop dockable windows"},
        new String[]{"JIDE Components", "Collection of useful components"},
        new String[]{"JIDE Grids", "Collection of JTable related components"},
        new String[]{"JIDE Dialogs", "Collection of JDialog related components", }
    };

    static String[][] JIDE_DOCK = new String[][]{
        new String[]{"Drag-n-drop", ""},
        new String[]{"Autohide Windows", ""},
        new String[]{"Nested Floating Windows", ""},
        new String[]{"Persist Layouts", ""}
    };

    static String[][] JIDE_COMP = new String[][]{
        new String[]{"DocumentPane", "Drag-n-drop tabbed document interface as you see in modern IDEs etc"},
        new String[]{"Status Bar", "Full customizable status bar that you can use to display status, progress, time and other information at the bottom of your application"},
        new String[]{"FloorTabbedPane", "Shortcut Bar, just like in Outlook"},
        new String[]{"CollapsiblePane", "Task Bar which you can find in Windows XP"}
    };

    static String[][] JIDE_GRIDS = new String[][]{
        new String[]{"PropertyTable", "A two-column JTable used to display properties of any object"},
        new String[]{"SortableTable", "A JTable which allows multiple-column sorting"},
        new String[]{"FilterTableModel", "A table model which allows filters on each column"},
        new String[]{"HierarchicalTable", "A JTable which allows hierarchical display of components on each row"},
        new String[]{"ComboBox", "A collection of ComboBoxes"},
        new String[]{"CellEditors", "A collection of CellEditor for many types of objects"},
        new String[]{"Converters", "A list of converters which can convert String to/from any other types"},
        new String[]{"TableUtils", "A utility class which contains several useful methods for JTable"}
    };

    static String[][] JIDE_COMBOBOXES = new String[][]{
        new String[]{"ColorComboBox", "A ComboBox to choose color"},
        new String[]{"DateComboBox", "A ComboBox to choose date"},
        new String[]{"MonthComboBox", "A ComboBox to choose month"},
        new String[]{"FileChooserComboBox", "A ComboBox to choose file"},
        new String[]{"FontComboBox", "A ComboBox to choose font"},
        new String[]{"ListComboBox", "A ComboBox to choose an item from a list"},
        new String[]{"BooleanComboBox", "A ComboBox to choose true or false"},
        new String[]{"StringArrayComboBox", "A ComboBox to choose String[]"}
    };

    static String[][] JIDE_CELLEDITORS = new String[][]{
        new String[]{"ColorCellEditor", "A cell editor to choose a color"},
        new String[]{"DateCellEditor", "A cell editor to choose a date"},
        new String[]{"MonthCellEditor", "A cell editor to choose a month"},
        new String[]{"FileNameCellEditor", "A cell editor to choose file name"},
        new String[]{"DoubleCellEditor", "A cell editor to edit double"},
        new String[]{"IntegerCellEditor", "A cell editor to edit int"},
        new String[]{"NumberCellEditor", "A cell editor to edit number"},
        new String[]{"ListComboBoxCellEditor", "A cell editor to choose an item from a list"},
        new String[]{"BooleanCellEditor", "A cell editor to choose true or false"},
        new String[]{"FontNameCellEditor", "A cell editor to choose a font name"},
        new String[]{"StringArrayCellEditor", "A cell editor to choose a String[]"}
    };

    static class GridsProductTableModel extends DefaultTableModel implements HierarchicalTableModel {

        public GridsProductTableModel() {
            super(JIDE_GRIDS, DETAIL_COLUMNS);
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public boolean hasChildComponent(int row) {
            return row == 4 || row == 5;
        }

        public boolean isHierarchical(int row) {
            return true;
        }

        public JComponent getChildComponent(int row) {
            TableModel model = null;
            if (row == 4) {
                model = new DefaultTableModel(JIDE_COMBOBOXES, DETAIL_COLUMNS) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
            }
            else if (row == 5) {
                model = new DefaultTableModel(JIDE_CELLEDITORS, DETAIL_COLUMNS) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
            }
            if (model != null) {
                SortableTable sortableTable = new SortableTable(model);
                FitScrollPane pane = new FitScrollPane(sortableTable);
                sortableTable.setBackground(BG3);
                Dimension size = sortableTable.getPreferredSize();
                pane.setPreferredSize(new Dimension(size.width, size.height + 24));
                return new TreeLikeHierarchicalPanel(pane);
            }
            else {
                return null;
            }
        }
    }

    static String[][] JIDE_DIALOGS = new String[][]{
        new String[]{"StandardDialog", "A common dialog class which implements some common features that all dialogs will need"},
        new String[]{"Lazy-loading Page", "Lazy loading panel with page event (open, closing, closed etc) support"},
        new String[]{"Wizard", "Wizard component which support both Microsoft Wizard 97 standard and Java L&F standard"},
        new String[]{"MultiplePageDialog", "A dialog contains many pages which can be used as options dialog or user preference dialog"},
        new String[]{"ButtonPanel", "Layout buttons in different order based OS convention"},
        new String[]{"BannerPanel", "A banner panel mainly for decoration purpose"},
        new String[]{"Tips of the Day Dialog", "A dialog shows tips of the day"}
    };

    static class ProductTableModel extends DefaultTableModel implements HierarchicalTableModel {

        public ProductTableModel() {
            super(JIDE_PRODUCTS, PRODUCT_COLUMNS);
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public boolean hasChildComponent(int row) {
            return true;
        }

        public boolean isHierarchical(int row) {
            return true;
        }

        public JComponent getChildComponent(int row) {
            TableModel model = null;
            switch (row) {
                case 0:
                    model = new DefaultTableModel(JIDE_DOCK, DETAIL_COLUMNS) {
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    break;
                case 1:
                    model = new DefaultTableModel(JIDE_COMP, DETAIL_COLUMNS) {
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    break;
                case 2:
                    model = new GridsProductTableModel();
                    HierarchicalTable table = new HierarchicalTable(model);
                    table.setBackground(BG2);
                    table.setOpaque(true);
                    table.setName("Detail Table");
                    return new TreeLikeHierarchicalPanel(new FitScrollPane(table));
                case 3:
                    model = new DefaultTableModel(JIDE_DIALOGS, DETAIL_COLUMNS) {
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    break;
            }

            if (model != null) {
                SortableTable sortableTable = new SortableTable(model);
                sortableTable.setBackground(BG2);
                return new TreeLikeHierarchicalPanel(new FitScrollPane(sortableTable));
            }
            else {
                return null;
            }
        }

    }

    static class FitScrollPane extends JScrollPane implements ComponentListener {
        public FitScrollPane() {
            initScrollPane();
        }

        public FitScrollPane(Component view) {
            super(view);
            initScrollPane();
        }

        public FitScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
            super(view, vsbPolicy, hsbPolicy);
            initScrollPane();
        }

        public FitScrollPane(int vsbPolicy, int hsbPolicy) {
            super(vsbPolicy, hsbPolicy);
            initScrollPane();
        }

        private void initScrollPane() {
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            getViewport().getView().addComponentListener(this);
        }

        public void componentResized(ComponentEvent e) {
            setSize(getSize().width, getPreferredSize().height);
            if (Lm.HG_DEBUG) {
                System.out.println("FitScrollPane resized " + this.getClass().getName());
            }
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }

        public void componentHidden(ComponentEvent e) {
        }

        public Dimension getPreferredSize() {
            getViewport().setPreferredSize(getViewport().getView().getPreferredSize());
            return super.getPreferredSize();
        }
    }
}
