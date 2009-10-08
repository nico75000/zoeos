package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 21-May-2003
 * Time: 01:50:06
 * To change this template use Options | File Templates.
 */
public class MasterParameterSelection extends AbstractE4Selection {
    private Integer[] ids;
    private Integer[] vals;
    private int category;

    public static final int MASTER_GENERAL = MASTER_BASE + 0;
    public static final int MASTER_TUNING = MASTER_BASE + 1;
    public static final int MASTER_IO = MASTER_BASE + 2;
    public static final int MASTER_SCSI_DISK = MASTER_BASE + 3;
    public static final int MASTER_IMPORT_OPTIONS = MASTER_BASE + 4;
    public static final int MASTER_MIDI_PREFERENCES = MASTER_BASE + 5;
    public static final int MASTER_MIDI_MODE = MASTER_BASE + 6;
    public static final int MASTER_MIDI_CONTROLLERS = MASTER_BASE + 7;
    public static final int MASTER_FX_SETUP = MASTER_BASE + 8;
    public static final int MASTER_FX_A = MASTER_BASE + 9;
    public static final int MASTER_FX_B = MASTER_BASE + 10;

    public static int convertMasterCategoryString(String c) {
        if (c.equals(ParameterCategories.MASTER_FX_SETUP))
            return MASTER_FX_SETUP;
        if (c.equals(ParameterCategories.MASTER_FX_A))
            return MASTER_FX_A;
        if (c.equals(ParameterCategories.MASTER_FX_B))
            return MASTER_FX_B;
        if (c.equals(ParameterCategories.MASTER_IMPORT_OPTIONS))
            return MASTER_IMPORT_OPTIONS;
        if (c.equals(ParameterCategories.MASTER_IO))
            return MASTER_IO;
        if (c.equals(ParameterCategories.MASTER_MIDI_CONTROLLERS))
            return MASTER_MIDI_CONTROLLERS;
        if (c.equals(ParameterCategories.MASTER_MIDI_MODE))
            return MASTER_MIDI_MODE;
        if (c.equals(ParameterCategories.MASTER_MIDI_PREFERENCES))
            return MASTER_MIDI_PREFERENCES;
        if (c.equals(ParameterCategories.MASTER_SCSI_DISK))
            return MASTER_SCSI_DISK;
        if (c.equals(ParameterCategories.MASTER_TUNING))
            return MASTER_TUNING;
        return MASTER_GENERAL;
    }

    public MasterParameterSelection(DeviceContext dev, PresetParameterSelection pps) {
        super(dev);
        Integer[] ids = pps.getIds();
        Integer[] vals = pps.getVals();

        ArrayList l_ids = new ArrayList();
        ArrayList l_vals = new ArrayList();

        for (int i = 0; i < ids.length; i++) {
            if (ids[i].intValue() >= 6 && ids[i].intValue() <= 21) {
                l_ids.add(ParameterUtilities.convertPresetToMasterFxId(ids[i]));
                l_vals.add(vals[i]);
            }
        }
        this.category = MASTER_GENERAL;
        this.ids = (Integer[]) l_ids.toArray(new Integer[l_ids.size()]);
        this.vals = (Integer[]) l_vals.toArray(new Integer[l_vals.size()]);
    }

    public MasterParameterSelection(DeviceContext dev, Integer[] ids, int category) throws ZDeviceNotRunningException, IllegalParameterIdException {
        super(dev);
        this.ids = new Integer[ids.length];
        this.vals = new Integer[ids.length];
        //this.valStrings = new String[ids.length];
        this.category = category;
        MasterContext mc = dev.getMasterContext();
        DeviceParameterContext dpc = dev.getDeviceParameterContext();
        this.ids = (Integer[]) ids.clone();
        vals = mc.getMasterParams(ids);

        /*for (int i = 0, n = ids.length; i < n; i++) {
            try {
                valStrings[i] = deviceParameterContext.getMasterContext().getParameterDescriptor(ids[i]).getStringForValue(vals[i]);
            } catch (ParameterValueOutOfRangeException e) {
                valStrings[i] = "error";
                e.printStackTrace();
            }
        } */
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void render(MasterContext mc) {
        for (int i = 0, n = ids.length; i < n; i++) {
            try {
                mc.setMasterParam(ids[i], vals[i]);
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer[] getIds() {
        return (Integer[]) ids.clone();
    }

    public Integer[] getVals() {
        return (Integer[]) vals.clone();
    }

    public boolean containsId(Integer id) {
        for (int i = 0,j = ids.length; i < j; i++)
            if (ids[i].equals(id))
                return true;
        return false;
    }

    /*public String[] getValStrings() {
        return (String[]) valStrings.clone();
    } */
}
