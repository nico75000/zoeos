package com.pcmsolutions.device.EMU.database.gui;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.Context;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.device.EMU.database.ContextLocation;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: paulmeehan
 * Date: 04-Sep-2004
 * Time: 22:19:41
 */
public class ContextLocationCombo extends JComboBox {
    Context context;
    Integer lowIndex;
    Integer highIndex;
    List<ContextLocation> locations;

    public ContextLocationCombo() {
    }

    public void init(Context context, Integer lowIndex, Integer highIndex) throws DeviceException {
        this.context = context;
        this.lowIndex = lowIndex;
        this.highIndex = highIndex;
        setupCombo();
    }

    protected void setupCombo() throws DeviceException {
        locations = context.getContextIndexNamesInRange(lowIndex, highIndex);
        this.setModel(new DefaultComboBoxModel(locations.toArray()));
    }

    public Context getContext() {
        return context;
    }

    public List<ContextLocation> getLocations() {
        ArrayList<ContextLocation> outNames = new ArrayList<ContextLocation>();
        outNames.addAll(locations);
        return outNames;
    }

    public boolean selectLocation(final Integer contextIndex) {
        int i = locations.indexOf(new Object(){
            public boolean equals(Object o) {
                return ((ContextLocation)o).getIndex().equals(contextIndex);
            }
        });
        if (i != -1)
            this.setSelectedIndex(i);
        return i != -1;
    }

    public ContextLocation getSelectedLocation(){
        return (ContextLocation)getSelectedItem();
    }
    public void selectFirstEmptyLocation() {
        Integer contextIndex;
        for (Iterator<ContextLocation> i = locations.iterator(); i.hasNext();) {
            contextIndex = i.next().getIndex();
            try {
                if (context.isEmpty(contextIndex)){
                    selectLocation(contextIndex);
                    break;
                }
            } catch (DeviceException e) {
            }
        }
    }

    public Integer getLowIndex() {
        return lowIndex;
    }

    public Integer getHighIndex() {
        return highIndex;
    }
}
