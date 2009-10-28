package com.pcmsolutions.device.EMU.E4.gui.parameter2;

import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.gui.MouseWheelSpinner;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.threads.Impl_ZThread;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-May-2003
 * Time: 08:56:27
 * To change this template use Options | File Templates.
 */
public class ParameterModelTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener, ChangeListener, ZDisposable {
    private JComponent currentEditor;
    private EditableParameterModel currPM;
    private JSpinner spinner;
    private JComboBox comboBox;
    private SpinnerListModel spinnerModel = new SpinnerListModel() {
        public void setValue(Object elt) {
            try {
                super.setValue(elt);
            } catch (IllegalArgumentException e) {
                Integer nearestIndex;
                try {
                    nearestIndex = currPM.getParameterDescriptor().getValueForString(elt.toString());
                    super.setValue( getList().get(nearestIndex.intValue() - currPM.getParameterDescriptor().getMinValue().intValue()));
                } catch (ParameterValueOutOfRangeException e1) {
                    throw e;
                }
            }
        }
    };
    private boolean spinnerIsReady;
    private boolean comboIsReady;

    public ParameterModelTableCellEditor(Color bg, Color fg) {
        spinner = new MouseWheelSpinner();

        spinner.setModel(spinnerModel);
        spinner.addChangeListener(this);
        comboBox = new JComboBox();
        comboBox.addActionListener(this);
        //comboBox.setMaximumRowCount(16);
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        //comboBox.setRenderer(new GeneralListCellRenderer(bg, fg));
    }

    public Object getCellEditorValue() {
        if (currentEditor instanceof JComboBox)
            return ((JComboBox) currentEditor).getSelectedItem();
        if (currentEditor instanceof JSpinner)
            return ((JSpinner) currentEditor).getValue();
        else
            return null;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        if (value instanceof EditableParameterModel) {
            if (currPM != null)
                currPM.removeChangeListener(this);
            currPM = ((EditableParameterModel) value);

            currPM.addChangeListener(this);
            if (currPM.getParameterDescriptor().shouldUseSpinner()) {
                spinnerIsReady = false;
                if (currPM.getShowUnits()) {
                    spinnerModel.setList(currPM.getParameterDescriptor().getStringList());
                    try {
                        spinnerModel.setValue(currPM.getValueString());
                    } catch (ParameterException e) {
                        e.printStackTrace();
                    }
                } else {
                    spinnerModel.setList(currPM.getParameterDescriptor().getUnitlessStringList());
                    try {
                        spinnerModel.setValue(currPM.getValueUnitlessString());
                    } catch (ParameterException e) {
                        e.printStackTrace();
                    }
                }
                spinnerIsReady = true;
                currentEditor = spinner;
            } else {
                comboIsReady = false;
                DefaultComboBoxModel cm;
                if (currPM.getShowUnits()) {
                    cm = new DefaultComboBoxModel(currPM.getParameterDescriptor().getStringList().toArray());
                    try {
                        cm.setSelectedItem(currPM.getValueString());
                    } catch (ParameterException e) {
                        e.printStackTrace();
                    }
                } else {
                    cm = new DefaultComboBoxModel(currPM.getParameterDescriptor().getUnitlessStringList().toArray());
                    try {
                        cm.setSelectedItem(currPM.getValueUnitlessString());
                    } catch (ParameterException e) {
                        e.printStackTrace();
                    }
                }
                comboBox.setModel(cm);
                comboIsReady = true;
                currentEditor = comboBox;
            }
        } else
            currentEditor = null;

        if (currentEditor != null)
            currentEditor.validate();

        return currentEditor;
    }

    public static boolean tryToggleCellAt(JTable table, int row, int column) {
        Object o = table.getValueAt(row, column);
        if (o instanceof EditableParameterModel) {
            final EditableParameterModel pm = ((EditableParameterModel) o);
            final GeneralParameterDescriptor pd;
            pd = pm.getParameterDescriptor();
            if (pd.getMinValue().intValue() == pd.getMaxValue().intValue() - 1) {
              //  Impl_ZThread.parameterTQ.postTask(new Impl_ZThread.Task() {
              //      public void doTask() {
                        try {
                            if (pm.getValue().equals(pd.getMinValue()))
                                pm.setValue(pd.getMaxValue());
                            else
                                pm.setValue(pd.getMinValue());
                        } catch (ParameterException e) {
                        }
             //       }
             //   });
                return true;
            }
        }
        return false;
    }

    public static boolean testCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            if (me.getClickCount() == 2)
                return true;
        }
        return false;
    }

    public boolean isCellEditable(EventObject e) {
        return testCellEditable(e);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public boolean stopCellEditing() {
        if (currPM != null)
            currPM.removeChangeListener(this);
        return super.stopCellEditing();
    }

    public void cancelCellEditing() {
        if (currPM != null)
            currPM.removeChangeListener(this);
        super.cancelCellEditing();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboBox && comboIsReady) {
            final boolean showUnits = currPM.getShowUnits();
            final String val = comboBox.getSelectedItem().toString();
          //  Impl_ZThread.parameterTQ.postTask(new Impl_ZThread.Task() {
           //     public void doTask() {
                    try {
                        if (showUnits)
                            currPM.setValueString(val);
                        else
                            currPM.setValueUnitlessString(val);
                    } catch (ParameterException e1) {
                        e1.printStackTrace();
                    }
           //     }
          //  });
        }
    }

    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
        if (src == spinner && spinnerIsReady) {
            final boolean showUnits = currPM.getShowUnits();
            final String val = spinner.getValue().toString();
          //  Impl_ZThread.parameterTQ.postTask(new Impl_ZThread.Task() {
            //    public void doTask() {
                    try {
                        if (showUnits)
                            currPM.setValueString(val);
                        else
                            currPM.setValueUnitlessString(val);
                    } catch (ParameterException e1) {
                        e1.printStackTrace();
                    }
           //     }
          //  });
        } else if (src instanceof EditableParameterModel) {
            if (currentEditor == spinner)
                try {
                    spinnerIsReady = false;
                    if (currPM.getShowUnits() == true)
                        spinner.setValue(((EditableParameterModel) src).getValueString());
                    else
                        spinner.setValue(((EditableParameterModel) src).getValueUnitlessString());
                } catch (ParameterException e1) {
                    e1.printStackTrace();
                } finally {
                    spinnerIsReady = true;
                }
            else if (currentEditor == comboBox)
                try {
                    comboIsReady = false;
                    if (currPM.getShowUnits() == true)
                        comboBox.setSelectedItem(((EditableParameterModel) src).getValueUnitlessString());
                    else
                        comboBox.setSelectedItem(((EditableParameterModel) src).getValueString());
                } catch (ParameterException e1) {
                    e1.printStackTrace();
                } finally {
                    comboIsReady = true;
                }
        }
    }

    public void zDispose() {
        if (currPM != null)
            currPM.removeChangeListener(this);
    }
}