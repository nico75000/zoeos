/*
 * @(#)${NAME}
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.combobox.FileChooserComboBox;
import com.jidesoft.combobox.FileChooserPanel;
import com.jidesoft.combobox.PopupPanel;
import com.jidesoft.combobox.ListComboBox;
import com.jidesoft.converter.ColorConverter;
import com.jidesoft.converter.ConverterContext;
import com.jidesoft.converter.MonthConverter;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.grid.*;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.utils.Lm;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Demoed Component: {@link com.jidesoft.grid.PropertyPane}, {@link com.jidesoft.grid.PropertyTable}
 * <br>
 * This demo shows you how to use EditorContext to pass in additional data so that cell editor can use it.
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class PropertyPaneEditorContextDemo extends JFrame {

    private static PropertyPaneEditorContextDemo _frame;
    private static PropertyTable _table;

    public PropertyPaneEditorContextDemo(String title) throws HeadlessException {
        super(title);
    }

    public PropertyPaneEditorContextDemo() throws HeadlessException {
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

        _frame = new PropertyPaneEditorContextDemo("PropertyPane Sample");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                _frame.dispose();
                System.exit(0);
            }
        });

        _table = createTable();

        PropertyPane propertyPane = new PropertyPane(_table);

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(propertyPane, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 400, 500);

        _frame.setVisible(true);

    }

    // create property table
    private static PropertyTable createTable() {
        EditorContext genericEditorContext = new EditorContext("Generic");
        CellEditorManager.registerEditor(String.class, new FormattedTextFieldCellEditor(String.class), genericEditorContext);

        ArrayList list = new ArrayList();

        SampleProperty property = null;

        property = new SampleProperty("two choices", "This row has two possible values", String.class, "Choices");
        genericEditorContext = new EditorContext("Generic", new String[] {"1", "2"});
        property.setEditorContext(genericEditorContext);
        list.add(property);

        property = new SampleProperty("three choices", "This row has three possible values", String.class, "Choices");
        genericEditorContext = new EditorContext("Generic", new String[] {"1", "2", "3"});
        property.setEditorContext(genericEditorContext);
        list.add(property);

        property = new SampleProperty("four choices", "This row has three possible values", String.class, "Choices");
        genericEditorContext = new EditorContext("Generic", new String[] {"1", "2", "3", "4"});
        property.setEditorContext(genericEditorContext);
        list.add(property);

        PropertyTableModel model = new PropertyTableModel(list);
        PropertyTable table = new PropertyTable(model);
        table.expandFirstLevel();
        return table;
    }

    static HashMap map = new HashMap();

    static {
        map.put("two choices", "2");
        map.put("three choices", "3");
        map.put("four choices", "4");
    }

    static class SampleProperty extends Property {
        public SampleProperty(String name, String description, Class type, String category, ConverterContext context, java.util.List childProperties) {
            super(name, description, type, category, context, childProperties);
        }

        public SampleProperty(String name, String description, Class type, String category, ConverterContext context) {
            super(name, description, type, category, context);
        }

        public SampleProperty(String name, String description, Class type, String category) {
            super(name, description, type, category);
        }

        public SampleProperty(String name, String description, Class type) {
            super(name, description, type);
        }

        public SampleProperty(String name, String description) {
            super(name, description);
        }

        public SampleProperty(String name) {
            super(name);
        }

        public void setValue(Object value) {
            if (Lm.PG_DEBUG) {
                System.out.println("(BeanProperty) Set value " + ObjectConverterManager.toString(value) + " of type " + (value != null ? value.getClass().getName() : "null"));
            }
            map.put(getFullName(), value);
        }

        public Object getValue() {
            Object value = map.get(getFullName());
            if (Lm.PG_DEBUG) {
                System.out.println("(BeanProperty) Get value " + ObjectConverterManager.toString(value));
            }
            return value;
        }

        public boolean hasValue() {
            return map.get(getFullName()) != null;
        }

        static GenericCellEditor _cellEditor = new GenericCellEditor();
        public CellEditor getCellEditor(int column) {
            if(column == 1) {
                return _cellEditor;
            }
            return super.getCellEditor(column);
        }
    }

    static class GenericCellEditor extends ListComboBoxCellEditor {
        public GenericCellEditor() {
            super(new Object[0]);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component component =  super.getTableCellEditorComponent(table, value, isSelected, row, column);
            if(component instanceof ListComboBox && table instanceof PropertyTable && table.getModel() instanceof PropertyTableModel) {
                PropertyTableModel model = (PropertyTableModel) table.getModel();
                Property property =  model.getPropertyAt(row);
                EditorContext context = property.getEditorContext();
                if(context.getUserObject() instanceof String[]) {
                    ((ListComboBox) component).setModel(new DefaultComboBoxModel((String[]) context.getUserObject()));
                    ((ListComboBox) component).setPopupVolatile(true);
                }
            }
            return component;
        }
    }
}
