package com.pcmsolutions.system;

import com.jidesoft.grid.CellEditorManager;
import com.jidesoft.grid.Property;
import com.pcmsolutions.system.preferences.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: paulmeehan
 * Date: 13-Feb-2004
 * Time: 12:06:43
 */
public class ZProperty extends Property implements ZDisposable {
    private ZPref pref;

    public ZProperty(ZBoolPref pref) {
        super(pref.getPresentationName(), pref.getDescription(), ZBoolPref.class, pref.getCategory());
        this.pref = pref;
    }

    public ZProperty(ZIntPref pref) {
        super(pref.getPresentationName(), pref.getDescription(), ZIntPref.class, pref.getCategory());
        this.pref = pref;
    }

    public ZProperty(ZDoublePref pref) {
        super(pref.getPresentationName(), pref.getDescription(), ZDoublePref.class, pref.getCategory());
        this.pref = pref;
    }

    public ZProperty(ZStringPref pref) {
        super(pref.getPresentationName(), pref.getDescription(), ZStringPref.class, pref.getCategory());
        this.pref = pref;
    }

    public ZProperty(ZEnumPref pref) {
        super(pref.getPresentationName(), pref.getDescription(), ZEnumPref.class, pref.getCategory());
        this.pref = pref;
    }

    private static class ZPrefEditor extends AbstractCellEditor implements TableCellEditor {
        JComboBox combo;
        JSpinner spinner;

        public Object getCellEditorValue() {
            if (combo != null)
                return combo.getSelectedItem();
            if (spinner != null)
                return spinner.getValue();
            return "";
        }

        public Component getTableCellEditorComponent(JTable table, final Object value,
                                                     boolean isSelected,
                                                     int row, int column) {
            combo = null;
            spinner = null;
            if (value instanceof ZBoolPref) {
                final ZBoolPref zp = (ZBoolPref) value;
                combo = new JComboBox(new Object[]{"true", "false"});
                combo.setSelectedItem(zp.getValueString());
                combo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        zp.putValueString(combo.getSelectedItem().toString());
                    }
                });
                return combo;
            } else if (value instanceof ZEnumPref) {
                final ZEnumPref zp = (ZEnumPref) value;
                combo = new JComboBox(zp.getLegalValues());
                combo.setSelectedItem(zp.getValue());
                combo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        zp.putValueString(combo.getSelectedItem().toString());
                    }
                });
                return combo;
            } else if (value instanceof ZIntPref) {
                final ZIntPref zp = (ZIntPref) value;
                if (zp.getMinValue() != Integer.MIN_VALUE || zp.getMaxValue() != Integer.MAX_VALUE || zp.getIncrementValue() != 1) {
                    spinner = new JSpinner(new SpinnerNumberModel(zp.getValue(), zp.getMinValue(), zp.getMaxValue(), zp.getIncrementValue()));
                    spinner.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            zp.putValueString(spinner.getValue().toString());
                        }
                    });
                    return spinner;
                }
            }
            CellEditor ce = CellEditorManager.getEditor(value.getClass());
            if (ce instanceof TableCellEditor)
                return ((TableCellEditor) ce).getTableCellEditorComponent(table, value, isSelected, row, column);
            else
                return null;
        }
    };
    private static final ZPrefEditor prefEditor = new ZPrefEditor();

    static {
        CellEditorManager.registerEditor(ZEnumPref.class, prefEditor);
        CellEditorManager.registerEditor(ZBoolPref.class, prefEditor);
        CellEditorManager.registerEditor(ZDoublePref.class, CellEditorManager.getEditor(Double.class));
        CellEditorManager.registerEditor(ZIntPref.class, prefEditor);
        CellEditorManager.registerEditor(ZStringPref.class, CellEditorManager.getEditor(String.class));
    }

    public void setValue(Object o) {
        pref.putValueString(o.toString());
    }

    public Object getValue() {
        return pref;
    }

    public boolean hasValue() {
        return true;
    }

    public ZPref getZPref() {
        return pref;
    }

    public void zDispose() {
        pref.zDispose();
    }
}
