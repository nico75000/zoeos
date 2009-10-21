/*
 * @(#)PropertyPaneDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.converter.ColorConverter;
import com.jidesoft.converter.ConverterContext;
import com.jidesoft.converter.MonthConverter;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.grid.*;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.utils.Lm;

import javax.swing.*;
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
 * Demoed Component: {@link PropertyPane}, {@link PropertyTable}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class PropertyPaneDemo extends JFrame {

    private static PropertyPaneDemo _frame;
    private static PropertyTable _table;

    public PropertyPaneDemo(String title) throws HeadlessException {
        super(title);
    }

    public PropertyPaneDemo() throws HeadlessException {
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

        _frame = new PropertyPaneDemo("PropertyPane Sample");
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
        // before use MaskFormatter, you must register converter context and editor context
        ConverterContext ssnConverterContext = null;
        try {
            ssnConverterContext = new ConverterContext("SSN", new MaskFormatter("###-##-####"));
        } catch (ParseException e) {
        }
        EditorContext ssnEditorContext = new EditorContext("SSN");
        CellEditorManager.registerEditor(String.class, new FormattedTextFieldCellEditor(String.class), ssnEditorContext);

        ArrayList list = new ArrayList();

        SampleProperty property = null;

        property = new SampleProperty("Background", "The row is intended to show how to create a cell to input color in RGB format.", Color.class, "Appearance");
        list.add(property);

        property = new SampleProperty("Foreground", "The row is intended to show how to create a cell to input color in HEX format.", Color.class, "Appearance", ColorConverter.CONTEXT_HEX);
        list.add(property);

        property = new SampleProperty("Opaque", "The row is intended to show how to create a cell to input a boolean value.", Boolean.class, "Appearance");
        list.add(property);

        property = new SampleProperty("Bounds", "The row is intended to show how to create a cell to input a Rectangle.", Rectangle.class, "Appearance");
        list.add(property);

        property = new SampleProperty("Dimension", "The row is intended to show how to create a cell to input a Dimension.", Dimension.class, "Appearance");
        list.add(property);

        property = new SampleProperty("Name", "Name of the component", String.class);
        property.setCategory("Appearance");
        list.add(property);

        property = new SampleProperty("Font Name", "The row is intended to show how to create a cell to choose a font name", String.class);
        property.setEditorContext(new EditorContext("FontName"));
        property.setConverterContext(new ConverterContext("FontName"));
        property.setCategory("Appearance");
        list.add(property);

        property = new SampleProperty("Text", "Text of the component", String.class);
        property.setCategory("Appearance");
        list.add(property);

        property = new SampleProperty("Visible", "Visibility", Boolean.class);
        property.setCategory("Appearance");
        list.add(property);

        property = new SampleProperty("File", "The row is intended to show how to create a cell to input a file using FileChooser.", File.class);
        list.add(property);

        property = new SampleProperty("CreationDate", "The row is intended to show how to create a cell to input Date using DateComboBox.", Date.class);
        list.add(property);

        property = new SampleProperty("ExpirationDate", "The row is intended to show how to create a cell to input month/year using MonthComboBox.", Calendar.class);
        property.setConverterContext(MonthConverter.CONTEXT_MONTH);
        property.setEditorContext(MonthCellEditor.CONTEXT);
        list.add(property);

        property = new SampleProperty("Not Editable", "The row is intended to show a readonly cell.");
        property.setEditable(false);
        list.add(property);

        property = new SampleProperty("Double", "The row is intended to show how to create a cell to input a double.", Double.class);
        list.add(property);

        property = new SampleProperty("Integer", "The row is intended to show how to create a cell to input an integer.", Integer.class);
        list.add(property);

        property = new SampleProperty("Long text to test tooltips. If you see the whole text, you are OK.", "The row is intended to show how to tooltip of the cell value is too long.");
        list.add(property);


        property = new SampleProperty("SSN", "This row is intended to show how to create a String cell editor which uses MaskFormatter",
                String.class);
        property.setConverterContext(ssnConverterContext);
        property.setEditorContext(ssnEditorContext);
        list.add(property);

        property = new SampleProperty("Level 1", "The row is intended to show how to create several levels.");

        SampleProperty property2 = new SampleProperty("Level 2");
        property.addChild(property2);

        SampleProperty property31 = new SampleProperty("Level 3.1");
        property2.addChild(property31);

        SampleProperty property32 = new SampleProperty("Level 3.2");
        property2.addChild(property32);

        SampleProperty property33 = new SampleProperty("You can have as many levels as you want.");
        property2.addChild(property33);

        list.add(property);

        PropertyTableModel model = new PropertyTableModel(list);
        PropertyTable table = new PropertyTable(model);

        table.expandFirstLevel();
        return table;
    }

    static HashMap map = new HashMap();

    static {
        map.put("Bounds", new Rectangle(0, 0, 100, 200));
        map.put("Dimension", new Dimension(800, 600));
        map.put("Background", new Color(255, 0, 0));
        map.put("Foreground", new Color(255, 255, 255));
        map.put("File", new File("C:\\Program Files\\JIDE\\src\\com\\jidesoft\\Demo.java"));
        map.put("CreationDate", Calendar.getInstance());
        map.put("ExpirationDate", Calendar.getInstance());
        map.put("Name", "Label1");
        map.put("Font Name", "Arial");
        map.put("Text", "Data");
        map.put("Opaque", Boolean.FALSE);
        map.put("Visible", Boolean.TRUE);
        map.put("Not Editable", new Integer(10));
        map.put("Integer", new Integer(1234));
        map.put("Double", new Double(1.0));
        map.put("SSN", "000-00-0000");
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
    }
}
