package com.pcmsolutions.system;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 10-Mar-2004
 * Time: 17:05:07
 */
public class TestTable extends JFrame {
    public TestTable() throws HeadlessException {
        JTable t = new JTable(new TableModel() {
            public int getRowCount() {
                return 2;
            }

            public int getColumnCount() {
                return 2;
            }

            public String getColumnName(int columnIndex) {
                return "";
            }

            public Class getColumnClass(int columnIndex) {
                return String.class;
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return "test";
            }

            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            }

            public void addTableModelListener(TableModelListener l) {
            }

            public void removeTableModelListener(TableModelListener l) {
            }
        });
        this.getContentPane().add(t);
        GeneralTableCellRenderer gr = new GeneralTableCellRenderer() {
            {
                setForeground(UIColors.getTableFirstSectionFG());
                setBackground(UIColors.getTableFirstSectionBG());
            }
        };
        t.setDefaultRenderer(Object.class, gr);
        pack();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LookAndFeelFactory.installJideExtension();
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (UnsupportedLookAndFeelException e) {
        }
        TestTable t = new TestTable();

        t.show();
    }
}
