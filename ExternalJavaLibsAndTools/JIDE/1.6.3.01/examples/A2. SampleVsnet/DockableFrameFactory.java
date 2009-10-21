/*
 * @(#)DockableFrameFactory.java
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.grid.*;
import com.jidesoft.converter.ConverterContext;
import com.jidesoft.converter.ColorConverter;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.utils.Lm;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.io.File;

/**
 */
public class DockableFrameFactory {
    public static DockableFrame createSampleProjectViewFrame() {
        DockableFrame frame = new DockableFrame("Project View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SOLUTION));
        frame.getContext().setInitMode(DockContext.STATE_AUTOHIDE);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    public static DockableFrame createSampleClassViewFrame() {
        DockableFrame frame = new DockableFrame("Class View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.CLASSVIEW));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
        frame.getContext().setInitIndex(1);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(400, 200));
        frame.setTitle("Class View - SampleVsnet");
        frame.setTabTitle("Class View");
        return frame;
    }

    public static DockableFrame createSampleServerFrame() {
        DockableFrame frame = new DockableFrame("Server Explorer", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.SERVER));
        frame.getContext().setInitMode(DockContext.STATE_AUTOHIDE);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
        frame.getContext().setInitIndex(0);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    public static DockableFrame createSampleResourceViewFrame() {
        DockableFrame frame = new DockableFrame("Resource View", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.RESOURCEVIEW));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
        frame.getContext().setInitIndex(1);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        frame.setTitle("Resource View");
        return frame;
    }

    public static DockableFrame createSamplePropertyFrame() {
        // initialization for JIDE Grids
        ObjectConverterManager.initDefaultConverter();
        CellEditorManager.initDefaultEditor();
        CellRendererManager.initDefaultRenderer();

        DockableFrame frame = new DockableFrame("Property", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.PROPERTY));
        PropertyTable table = createTable();

        Font controlFont = UIManager.getFont("Table.font");
        table.setFont(controlFont);

        PropertyPane propertyPane = new PropertyPane(table);
        frame.setTitleBarComponent(propertyPane.getToolBar());
        propertyPane.setShowToolBar(false);
        propertyPane.setFont(controlFont);

        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
        frame.getContext().setInitIndex(0);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(propertyPane, BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension(200, 200));
        frame.setDefaultFocusComponent(table);
        return frame;
    }

    public static DockableFrame createSampleTaskListFrame() {
        DockableFrame frame = new DockableFrame("Task List", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.TASKLIST));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        JList list = new JList(new String[]{"Task1", "Task2", "Task3"});
        list.setToolTipText("This is a tooltip");
        frame.getContentPane().add(createScrollPane(list));
        frame.setPreferredSize(new Dimension(200, 200));
        frame.setMinimumSize(new Dimension(100, 100));
        return frame;
    }

    public static DockableFrame createSampleCommandFrame() {
        DockableFrame frame = new DockableFrame("Command", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.COMMAND));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(1);
        JTextArea textArea = new JTextArea();
        frame.getContentPane().add(createScrollPane(textArea));
        textArea.setText(">");
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    public static DockableFrame createSampleOutputFrame() {
        DockableFrame frame = new DockableFrame("Output", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.OUTPUT));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(0);
        frame.getContentPane().add(createScrollPane(new JTextArea()));
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    public static DockableFrame createSampleFindResult1Frame() {
        DockableFrame frame = new DockableFrame("Find Results 1", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FINDRESULT1));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(0);
        JTextArea textArea = new JTextArea();
        frame.getContentPane().add(createScrollPane(textArea));
        textArea.setText("Find all \"TestDock\", Match case, Whole word, Find Results 1, All Open Documents\n" +
                "C:\\Projects\\src\\com\\jidesoft\\test\\TestDock.java(1):// TestDock.java : implementation of the TestDock class\n" +
                "C:\\Projects\\src\\jidesoft\\test\\TestDock.java(8):#import com.jidesoft.test.TestDock;\n" +
                "C:\\Projects\\src\\com\\jidesoft\\Test.java(10):#import com.jidesoft.test.TestDock;\n" +
                "Total found: 3    Matching files: 5    Total files searched: 5");
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    public static DockableFrame createSampleFindResult2Frame() {
        DockableFrame frame = new DockableFrame("Find Results 2", VsnetIconsFactory.getImageIcon(VsnetIconsFactory.Standard.FINDRESULT2));
        frame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
        frame.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        frame.getContext().setInitIndex(1);
        JTextArea textArea = new JTextArea();
        frame.getContentPane().add(createScrollPane(textArea));
        textArea.setText("Find all \"TestDock\", Match case, Whole word, Find Results 2, All Open Documents\n" +
                "C:\\Projects\\src\\com\\jidesoft\\test\\TestDock.java(1):// TestDock.java : implementation of the TestDock class\n" +
                "C:\\Projects\\src\\jidesoft\\test\\TestDock.java(8):#import com.jidesoft.test.TestDock;\n" +
                "C:\\Projects\\src\\com\\jidesoft\\Test.java(10):#import com.jidesoft.test.TestDock;\n" +
                "Total found: 3    Matching files: 5    Total files searched: 5");
        frame.setPreferredSize(new Dimension(200, 200));
        return frame;
    }

    public static JScrollPane createScrollPane(Component component) {
        JScrollPane pane = new JideScrollPane(component);
        pane.setFocusable(false);
        return pane;
    }


    // create property table
    private static PropertyTable createTable() {
        // before use MaskFormatter, you must register converter context and editor context
        ConverterContext ssnConverterContext = null;
        try {
            ssnConverterContext = new ConverterContext("SSN", new MaskFormatter("###-##-####"));
        }
        catch (ParseException e) {
        }
        EditorContext ssnEditorContext = new EditorContext("SSN");
        CellEditorManager.registerEditor(String.class, new FormattedTextFieldCellEditor(String.class), ssnEditorContext);

        ArrayList list = new ArrayList();

        SampleProperty property = new SampleProperty("Background", "The row is intended to show how to create a cell to input color in RGB format.", Color.class, "Appearance");
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

        property = new SampleProperty("CreationDate", "The row is intended to show how to create a cell to input Date using DateComboBox.", Calendar.class);
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

        property = new SampleProperty("String Array", "This row is intended to show how to create a String array cell editor",
                String[].class);

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
                System.out.println("(BeanProperty) Set value " + value);
            }
            map.put(getFullName(), value);
        }

        public Object getValue() {
            Object value = map.get(getFullName());
            if (Lm.PG_DEBUG) {
                System.out.println("(BeanProperty) Get value " + value);
            }
            return value;
        }

        public boolean hasValue() {
            return map.get(getFullName()) != null;
        }
    }}
