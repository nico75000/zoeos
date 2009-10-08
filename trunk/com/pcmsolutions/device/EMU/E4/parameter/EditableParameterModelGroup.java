package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.system.ZCommand;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
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

    public String toString() {
        return group[0].toString();
    }

    public boolean equals(Object obj) {
        return group[0].equals(obj);
    }

    private void assertGroup() {
        if (group.length < 1)
            throw new IllegalArgumentException("EditableParameterModelGroup needs at least one parameter model");

        pd = group[0].getParameterDescriptor();
        for (int i = 1,j = group.length; i < j; i++) {
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

    public Integer getValue() throws ParameterUnavailableException {
        return group[0].getValue();
    }

    public void setTipShowingOwner(boolean tipShowsOwner) {
        group[0].setTipShowingOwner(tipShowsOwner);
    }

    public boolean isTipShowingOwner() {
        return group[0].isTipShowingOwner();
    }

    public String getValueString() throws ParameterUnavailableException {
        return group[0].getValueString();
    }

    public String getValueUnitlessString() throws ParameterUnavailableException {
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

    public ZCommand[] getZCommands() {
        return cmdProviderHelper.getCommandObjects(this);
    }

    /*private void offsetValue(int amt) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        int newVal;
        for (int i = 0,j = group.length; i < j; i++) {
            newVal = group[i].getValueObject().intValue() + amt;
            if (newVal > max)
                newVal = max;
            else if (newVal < min)
                newVal = min;
            try {
                group[i].setValueObject(IntPool.get(newVal));
            } catch (ParameterUnavailableException e) {
                if (i == 0) // tolerate any voice except the leading voice of the group
                    throw e;
            }
        }
    } */

    public void setValue(final Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        //   if (offsetting)
        //     offsetValue(value.intValue() - group[0].getValueObject().intValue());
        // else {
        group[0].setValue(new EditableParameterModel.EditChainValueProvider() {
            // might return null to signify no operation
            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                return value;
            }
        }, group);
        // }
    }

    public void setValueString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        setValue(pd.getValueForString(value));
    }

    public void setValueUnitlessString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        setValue(pd.getValueForUnitlessString(value));
    }

    public void defaultValue() throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        ParameterModelUtilities.defaultParameterModels(group);
    }

    public void setValue(EditChainValueProvider ecvp, EditableParameterModel[] modelChain) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
        if (modelChain[0] != this)
            throw new IllegalArgumentException("first element of passed chain must be this EditableParameterModelGroup");
        EditableParameterModel[] newChain = new EditableParameterModel[modelChain.length + group.length - 1];
        System.arraycopy(group, 0, newChain, 0, group.length);
        System.arraycopy(modelChain, 1, newChain, group.length, modelChain.length - 1);
        newChain[0].setValue(ecvp, newChain);
    }

    public boolean isEditChainableWith(Object o) {
        return ((EditableParameterModel) group[0]).isEditChainableWith(o);
    }

    public void stateChanged(ChangeEvent e) {
        for (int i = 0, j = changeListeners.size(); i < j; i++)
            ((ChangeListener) changeListeners.get(i)).stateChanged(new ChangeEvent(this));
    }

    public Icon getIcon() {
        return group[0].getIcon();
    }

    public String getToolTipText() {
        return group[0].getToolTipText();
    }

    public Object[] getWrappedObjects() {
        return (Object[]) group.clone();
    }
}
