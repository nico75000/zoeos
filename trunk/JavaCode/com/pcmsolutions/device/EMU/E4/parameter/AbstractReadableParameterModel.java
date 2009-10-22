package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.device.EMU.DeviceException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 26-Apr-2003
 * Time: 21:44:25
 * To change this template use Options | File Templates.
 */
public abstract class AbstractReadableParameterModel implements ReadableParameterModel, ZCommandProvider, ZDisposable {
    protected GeneralParameterDescriptor pd;
    transient private Vector changeListeners;
    protected boolean showUnits = true;
    protected boolean tipShowingOwner = false;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        makeTransients();
    }

    private void makeTransients() {
        changeListeners = new Vector();
    }

    public AbstractReadableParameterModel(GeneralParameterDescriptor pd) {
        this.pd = pd;
        makeTransients();
    }


    public boolean isTipShowingOwner() {
        return tipShowingOwner;
    }

    public void setTipShowingOwner(boolean tipShowingOwner) {
        this.tipShowingOwner = tipShowingOwner;
    }

    public void zDispose() {
        changeListeners.clear();
    }

    public boolean equals(Object obj) {
        if (obj instanceof ReadableParameterModel)
            return pd.getId().equals(((ReadableParameterModel) obj).getParameterDescriptor().getId());
        else if (obj instanceof Integer)
            return obj.equals(pd.getId());
        return false;
    }

    public abstract Integer getValue() throws ParameterException;

    public String toString() {
        try {
            if (showUnits)
                return pd.getStringForValue(getValue());
            else
                return pd.getUnitlessStringForValue(getValue());
        } catch (ParameterException e) {
        }
        return "";
    }

    public String getValueString() throws ParameterException {
        try {
            return pd.getStringForValue(getValue());
        } catch (ParameterValueOutOfRangeException e) {
            e.printStackTrace();
            throw new ParameterUnavailableException(pd.getId());
        }
    }

    public String getValueUnitlessString() throws ParameterException {
            return pd.getUnitlessStringForValue(getValue());        
    }

    public GeneralParameterDescriptor getParameterDescriptor() {
        return pd;
    }

    public void addChangeListener(ChangeListener cl) {
        changeListeners.add(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        changeListeners.remove(cl);
    }

    public void setShowUnits(boolean showUnits) {
        this.showUnits = showUnits;
    }

    public boolean getShowUnits() {
        return showUnits;
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return ReadableParameterModel.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    public Class[] getZCommandMarkers() {
        return ReadableParameterModel.cmdProviderHelper.getSupportedMarkers();
    }

    protected void fireChanged() {
        synchronized (changeListeners) {
            int size = changeListeners.size();
            for (int n = 0; n < size; n++) {
                try {
                    ((ChangeListener) changeListeners.get(n)).stateChanged(new ChangeEvent(this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Icon getIcon() {
        return null;
    }

    public String getToolTipText() {
        if (!tipShowingOwner)
            try {
                String s=  pd.getTipForValue(getValue());
                if (s == null)
                    return toString();
                else
                    return s;
            } catch (ParameterValueOutOfRangeException e) {
                // e.printStackTrace();
            } catch (ParameterUnavailableException e) {
                //e.printStackTrace();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        return null;
    }
}
