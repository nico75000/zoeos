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
import com.jidesoft.utils.SystemInfo;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Demoed Component: {@link com.jidesoft.grid.PropertyPane}, {@link com.jidesoft.grid.PropertyTable}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class TreeTableDemo3 extends JFrame {

    private static TreeTableDemo3 _frame;
    private static PropertyTable _table;

    public TreeTableDemo3(String title) throws HeadlessException {
        super(title);
    }

    public TreeTableDemo3() throws HeadlessException {
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

        _frame = new TreeTableDemo3("PropertyPane Sample");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                _frame.dispose();
                System.exit(0);
            }
        });

        _table = createTable();

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(new JScrollPane(_table), BorderLayout.CENTER);

        _frame.setBounds(10, 10, 400, 500);

        _frame.setVisible(true);

    }

    // create property table
    private static PropertyTable createTable() {
        // before use MaskFormatter, you must register converter context and editor context
        List list = new ArrayList();

        Task task = new Task("Task 1");
        Task subtask = null;
        Task subSubtask = null;
        list.add(task);

        subtask = new Task("Subtask 1.1");
        task.addChild(subtask);
        subtask = new Task("Subtask 1.2");
        task.addChild(subtask);
        subtask = new Task("Subtask 1.3");
        task.addChild(subtask);

        subSubtask = new Task("Subtask 1.3.1");
        subtask.addChild(subSubtask);
        subSubtask = new Task("Subtask 1.3.2");
        subtask.addChild(subSubtask);
        subSubtask = new Task("Subtask 1.3.3");
        subtask.addChild(subSubtask);

        task = new Task("Task 2");
        list.add(task);

        subtask = new Task("Subtask 2.1");
        task.addChild(subtask);
        subtask = new Task("Subtask 2.2");
        task.addChild(subtask);
        subtask = new Task("Subtask 2.3");
        task.addChild(subtask);

        task = new Task("Task 3");
        list.add(task);

        subtask = new Task("Subtask 2.1");
        task.addChild(subtask);
        subtask = new Task("Subtask 2.2");
        task.addChild(subtask);
        subtask = new Task("Subtask 2.3");
        task.addChild(subtask);

        PropertyTableModel model = new TaskTableModel(list);
        model.setOrder(PropertyTableModel.UNSORTED);
        PropertyTable table = new PropertyTable(model);

        table.expandFirstLevel();
        return table;
    }

    static class Task extends Property {
        private Color _flag = Color.black;
        private Calendar _dueDate = Calendar.getInstance();

        public Task(String name, String description, Class type, String category, ConverterContext context, java.util.List childProperties) {
            super(name, description, type, category, context, childProperties);
        }

        public Task(String name, String description, Class type, String category, ConverterContext context) {
            super(name, description, type, category, context);
        }

        public Task(String name, String description, Class type, String category) {
            super(name, description, type, category);
        }

        public Task(String name, String description, Class type) {
            super(name, description, type);
        }

        public Task(String name, String description) {
            super(name, description);
        }

        public Task(String name) {
            super(name);
        }

        public void setValue(Object value) {
        }

        public Object getValue() {
            return "";
        }

        public boolean hasValue() {
            return true;
        }

        public Color getFlag() {
            return _flag;
        }

        public void setFlag(Color flag) {
            _flag = flag;
        }

        public Calendar getDueDate() {
            return _dueDate;
        }

        public void setDueDate(Calendar dueDate) {
            _dueDate = dueDate;
        }

        public CellEditor getCellEditor(int column) {
            if(column == 2) {
                return CellEditorManager.getEditor(Color.class);
            }
            else if(column == 3) {
                return CellEditorManager.getEditor(Calendar.class);
            }
            else {
                return super.getCellEditor(column);    //To change body of overridden methods use File | Settings | File Templates.
            }
        }

        public TableCellRenderer getTableCellRenderer(int column) {
            if(column == 2) {
                return CellRendererManager.getRenderer(Color.class);
            }
            else if(column == 3) {
                return CellRendererManager.getRenderer(Calendar.class);
            }
            else {
                return super.getTableCellRenderer(column);
            }
        }
    }

    static class TaskTableModel extends PropertyTableModel {
        public TaskTableModel(List properties) {
            super(properties);
        }

        public Class getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return Color.class;
                case 3:
                    return Calendar.class;
            }
            return String.class;
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Task Name";
                case 1:
                    return "Description";
                case 2:
                    return "Color Flag";
                case 3:
                    return "Due Date";
            }
            return "";
        }

        public int getColumnCount() {
            return 4;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Property property = getPropertyAt(rowIndex);
            if (property == null) {
                return "-";
            }
            if (property instanceof Task) {
                Task task = (Task) property;
                switch (columnIndex) {
                    case 0:
                        return task.getName();
                    case 1:
                        return task.getDescription();
                    case 2:
                        return task.getFlag();
                    case 3:
                        return task.getDueDate();
                }
            }
            else {
                if(columnIndex == 0) {
                    return property.getName();
                }
            }
            return "";
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Task property = (Task) getPropertyAt(rowIndex);
            if (property == null) {
                return;
            }
            switch (columnIndex) {
                case 0:
                    property.setName("" + value);
                    break;
                case 1:
                    property.setDescription("" + value);
                    break;
                case 2:
                    property.setFlag((Color) value);
                    break;
                case 3:
                    property.setDueDate((Calendar) value);
                    break;
            }
        }
    }
}
