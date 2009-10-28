package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 18-May-2003
 * Time: 02:41:59
 * To change this template use Options | File Templates.
 */
public class ParameterModelEncloser implements EditableParameterModel, ZDisposable, ZCommandProvider {
    private EditableParameterModel parameterModel;

    public ParameterModelEncloser(EditableParameterModel p) {
        this.parameterModel = p;
    }

    public String toString() {
        return parameterModel.toString();
    }

    public boolean equals(Object o) {
        return parameterModel.equals(o);
    }

    public void zDispose() {
        parameterModel.zDispose();
        parameterModel = null;
    }

    public void setValue(Integer value) throws ParameterException {
        parameterModel.setValue(value);
    }

    public void offsetValue(Integer offset) throws ParameterException {
        parameterModel.offsetValue(offset);
    }

    public void offsetValue(Double offsetAsFOR) throws ParameterException {
        parameterModel.offsetValue(offsetAsFOR);
    }

    public void setValueString(String value) throws ParameterException{
        parameterModel.setValueString(value);
    }

    public void setValueUnitlessString(String value) throws ParameterException {
        parameterModel.setValueUnitlessString(value);
    }

    public Integer getValue() throws ParameterException {
        return parameterModel.getValue();
    }

    public void setTipShowingOwner(boolean tipShowsOwner) {
        parameterModel.setTipShowingOwner(tipShowsOwner);
    }

    public boolean isTipShowingOwner() {
        return parameterModel.isTipShowingOwner();
    }

    public String getValueString() throws ParameterException {
        return parameterModel.getValueString();
    }

    public String getValueUnitlessString() throws ParameterException {
        return parameterModel.getValueUnitlessString();
    }

    public void defaultValue() throws ParameterException {
        parameterModel.defaultValue();
    }

    public GeneralParameterDescriptor getParameterDescriptor() {
        return parameterModel.getParameterDescriptor();
    }

    public void addChangeListener(ChangeListener cl) {
        parameterModel.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        parameterModel.removeChangeListener(cl);
    }

    public void setShowUnits(boolean showUnits) {
        parameterModel.setShowUnits(showUnits);
    }

    public boolean getShowUnits() {
        return parameterModel.getShowUnits();
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return EditableParameterModel.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return EditableParameterModel.cmdProviderHelper.getSupportedMarkers();
    }

    public Icon getIcon() {
        return parameterModel.getIcon();
    }

    public String getToolTipText() {
        return parameterModel.getToolTipText();
    }
}
