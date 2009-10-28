package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.system.ZCommand;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Vector;

public class EditableParameterModelGroup implements EditableParameterModel, ChangeListener, ParameterModelWrapper {
    private EditableParameterModel[] group;
    private GeneralParameterDescriptor pd;
    private boolean offsetting = false;
    transient private Vector changeListeners;
    protected boolean expired = false;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        makeTransients();
    }

    private void makeTransients() {
        changeListeners = new Vector();
    }

    public EditableParameterModelGroup(EditableParameterModel[] group) {
        this.group = group;
        makeTransients();
        assertGroup();
        group[0].addChangeListener(this);
    }

    public boolean equals(Object obj) {
        return group[0].equals(obj);
    }

    private void assertGroup() {
        if (group.length < 1)
            throw new IllegalArgumentException("EditableParameterModelGroup needs at least one parameter model");

        pd = group[0].getParameterDescriptor();
        for (int i = 1, j = group.length; i < j; i++) {
            if (!group[i].getParameterDescriptor().equals(pd))
                throw new IllegalArgumentException("EditableParameterModelGroup requires all parameter models to have the same parameter descriptor");
            //if (!group[i].isEditChainableWith(group[i - 1]))
            //   throw new IllegalArgumentException("EditableParameterModelGroup requires all parameter models to be edit chainable with each other");
        }
    }

    public boolean isOffsetting() {
        return offsetting;
    }

    public void setOffsetting(boolean offsetting) {
        throw new UnsupportedOperationException("offsetting not currently supported in parameter groups");
        //this.offsetting = offsetting;
    }

    public Integer getValue() throws ParameterException {
        return group[0].getValue();
    }

    public void setTipShowingOwner(boolean tipShowsOwner) {
        group[0].setTipShowingOwner(tipShowsOwner);
    }

    public boolean isTipShowingOwner() {
        return group[0].isTipShowingOwner();
    }

    public String getValueString() throws ParameterException {
        return group[0].getValueString();
    }

    public String getValueUnitlessString() throws ParameterException {
        return group[0].getValueUnitlessString();
    }

    public GeneralParameterDescriptor getParameterDescriptor() {
        return pd;
    }

    public void addChangeListener(ChangeListener cl) {
        //group[0].addChangeListener(cl);
        changeListeners.add(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        //group[0].removeChangeListener(cl);
        changeListeners.remove(cl);
    }

    public void setShowUnits(boolean showUnits) {
        group[0].setShowUnits(showUnits);
    }

    public boolean getShowUnits() {
        return group[0].getShowUnits();
    }

    public void zDispose() {
        group[0].removeChangeListener(this);
        for (int i = 0; i < group.length; i++)
            group[i].zDispose();
        changeListeners.clear();
        group = null;
        changeListeners = null;
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return EditableParameterModel.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return EditableParameterModel.cmdProviderHelper.getSupportedMarkers();
    }

    public void setValue(final Integer value) throws ParameterException {
        for (int i = 0; i < group.length; i++)
            group[i].setValue(value);
    }

    public void offsetValue(Integer offset) throws ParameterException {
        for (int i = 0; i < group.length; i++)
            group[i].offsetValue(offset);
    }

    public void offsetValue(Double offsetAsFOR) throws ParameterException {
        for (int i = 0; i < group.length; i++)
            group[i].offsetValue(offsetAsFOR);
    }

    public void setValueString(String value) throws ParameterException {
        setValue(pd.getValueForString(value));
    }

    public void setValueUnitlessString(String value) throws ParameterException {
        setValue(pd.getValueForUnitlessString(value));
    }

    public void defaultValue() throws ParameterException {
        ParameterModelUtilities.defaultParameterModels(group);
    }

    public void stateChanged(ChangeEvent e) {
        for (int i = 0, j = changeListeners.size(); i < j; i++)
            ((ChangeListener) changeListeners.get(i)).stateChanged(new ChangeEvent(this));
    }

    public Icon getIcon() {
        return group[0].getIcon();
    }

    public String toString() {
        if (group.length == 1)
            return group[0].toString();
        else
            return group[0].toString();
    }

    public String getToolTipText() {
        if (group.length == 1)
            return group[0].getToolTipText();
        else {
            HashSet s = new HashSet();
            for (int i = 0; i < group.length; i++)
                try {
                    s.add(group[i].getValue());
                } catch (ParameterException e) {
                }
            StringBuffer sb = new StringBuffer();
            sb.append(group[0].getToolTipText()).append("  (").append(+s.size()).append(" distinct value").append((s.size() == 1 ? "" : "s")).append( ")");
            return sb.toString();
        }
    }

    public Object[] getWrappedObjects() {
        return (Object[]) group.clone();
    }
}
