package com.pcmsolutions.device.EMU.E4.gui.sample;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.system.ZDeviceNotRunningException;
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

public abstract class AbstractSampleTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener, ZDisposable {
    protected final JComboBox comboBox = new JComboBox();
    protected DefaultComboBoxModel comboBoxModel;
    protected DeviceContext deviceContext;
    protected List sampleItems;
    protected boolean comboModelInitialized;
    protected int negativeSampleCount = 0;

    public static final String SAMPLING_ADC = "Sampling ADC";
    public static final String AES_EBU_INPUT = "AES/EBU Input";
    public static final String EXTERNAL_ADC1 = "External ADC 1";
    public static final String EXTERNAL_ADC2 = "External ADC 2";
    public static final String ADAT_1_2 = "ADAT chan 1-2";
    public static final String ADAT_3_4 = "ADAT chan 3-4";
    public static final String ADAT_5_6 = "ADAT chan 5-6";
    public static final String ADAT_7_8 = "ADAT chan 7-8";


    public AbstractSampleTableCellEditor(DeviceContext d, Color bg, Color fg) throws ZDeviceNotRunningException {
        this.deviceContext = d;
        buildSampleItemsList(d);

        comboBoxModel = new DefaultComboBoxModel(sampleItems.toArray());
        comboBox.setModel(comboBoxModel);
        //comboBox.setRenderer(new GeneralListCellRenderer(bg, fg));
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        comboBox.addActionListener(this);
    }

    public static String subZeroIndexToString(int index) {
        switch (index) {
            case -1:
                return SAMPLING_ADC;
            case -2:
                return AES_EBU_INPUT;
            case -3:
                return EXTERNAL_ADC1;
            case -4:
                return EXTERNAL_ADC2;
            case -5:
                return ADAT_1_2;
            case -6:
                return ADAT_3_4;
            case -7:
                return ADAT_5_6;
            case -8:
                return ADAT_7_8;
            default:
                return "";
        }
    }

    public static int subZeroStringToIndex(String s) {
        if (s.equals(SAMPLING_ADC)) return -1;
        if (s.equals(AES_EBU_INPUT)) return -2;
        if (s.equals(EXTERNAL_ADC1)) return -3;
        if (s.equals(EXTERNAL_ADC2)) return -4;
        if (s.equals(ADAT_1_2)) return -5;
        if (s.equals(ADAT_3_4)) return -6;
        if (s.equals(ADAT_5_6)) return -7;
        if (s.equals(ADAT_7_8)) return -8;

        return Integer.MIN_VALUE;
    }

    protected void buildSampleItemsList(DeviceContext d) throws ZDeviceNotRunningException {
        try {
            sampleItems = new ArrayList();
            sampleItems.addAll(d.getDefaultSampleContext().getDatabaseSamples());
        } catch (NoSuchContextException e) {
            sampleItems = new ArrayList();
        }
    }

    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        comboModelInitialized = false;
        setSelectedIndex(getSelectedIndex());
        comboModelInitialized = true;
        return comboBox;
    }

    protected abstract Integer getSelectedIndex();

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboBox)
            if (comboModelInitialized)
                doAction(comboBox.getSelectedItem());
    }

    protected abstract void doAction(Object selection);

    protected void setSelectedIndex(Integer sample) {
        comboBoxModel.setSelectedItem(sampleItems.get(sample.intValue()));
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
