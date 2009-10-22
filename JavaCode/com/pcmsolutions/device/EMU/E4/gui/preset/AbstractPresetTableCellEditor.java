package com.pcmsolutions.device.EMU.E4.gui.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public abstract class AbstractPresetTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener, ZDisposable {
    private final JComboBox comboBox = new JComboBox();
    private DefaultComboBoxModel comboBoxModel;
    private DeviceContext d;
    protected List presetItems;
    private boolean comboModelInitialized;

    public AbstractPresetTableCellEditor(DeviceContext d, Color bg, Color fg)  {
        this.d = d;

        presetItems = buildPresetItemList();
        comboBoxModel = new DefaultComboBoxModel(presetItems.toArray());
        comboBox.setModel(comboBoxModel);
        //comboBox.setRenderer(new GeneralListCellRenderer(bg, fg));
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        comboBox.addActionListener(this);
    }

    protected List buildPresetItemList()  {
        try {
            return d.getDefaultPresetContext().getDatabasePresets();
        } catch (DeviceException e) {
            return new ArrayList();
        }
    }

    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        comboModelInitialized = false;
        try {
            setSelectedIndex(getSelectedIndex());
        } catch (DeviceException e) {
            e.printStackTrace();
        } catch (ParameterException e) {
            e.printStackTrace();
        }
        comboModelInitialized = true;
        return comboBox;
    }

    protected abstract Integer getSelectedIndex() throws DeviceException, ParameterException;

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboBox)
            if (comboModelInitialized) {
                doAction(comboBox.getSelectedItem());
            }
    }

    protected abstract void doAction(Object selection);

    protected void setSelectedIndex(Integer index) {
        comboBoxModel.setSelectedItem(presetItems.get(index.intValue()));
    }

    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            if (me.getClickCount() >= 2) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }
}
