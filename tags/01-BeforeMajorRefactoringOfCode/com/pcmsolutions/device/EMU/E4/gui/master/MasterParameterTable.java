package com.pcmsolutions.device.EMU.E4.gui.master;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.PresetParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.selections.MasterParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import java.util.ArrayList;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Jun-2003
 * Time: 22:20:30
 * To change this template use Options | File Templates.
 */
public class MasterParameterTable extends AbstractRowHeaderedAndSectionedTable implements MasterParameterSelectionAcceptor {
    protected String title;
    //protected static MasterTransferHandler masterTransferHandler = new MasterTransferHandler();
    protected String category;
    protected DeviceContext deviceContext;

    public void zDispose() {
        super.zDispose();
        deviceContext = null;
    }

    public String getCategory() {
        return category;
    }

    public MasterParameterTable(DeviceContext dc, EditableParameterModel[] parameterModels, String title, String category) throws ZDeviceNotRunningException {
        this(dc, new MasterParameterTableModel(parameterModels), title, category);
    }

    public MasterParameterTable(DeviceContext dc, MasterParameterTableModel tm, String title, String category) throws ZDeviceNotRunningException {
        super(tm, MasterTransferHandler.getInstance(), null, title + " >");
        this.title = title;
        this.deviceContext = dc;
        this.setDropChecker(new DropChecker() {
            public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                if (chosenDropFlavor instanceof MasterTransferHandler.MasterDataFlavor)
                    return ((MasterTransferHandler.MasterDataFlavor) chosenDropFlavor).containsId(((MasterParameterTableModel) getModel()).getIdForRow(row));
                if (chosenDropFlavor instanceof PresetParameterTableTransferHandler.PresetParameterDataFlavor)
                    return ((PresetParameterTableTransferHandler.PresetParameterDataFlavor) chosenDropFlavor).containsId(ParameterUtilities.convertMasterToPresetFxId(((MasterParameterTableModel) getModel()).getIdForRow(row)));
                return false;
            }
        });
        this.category = category;
        this.setHidingSelectionOnFocusLost(true);
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
    }

    protected void setupDropOverExtent() {
        dropOverExtent = -1; // whole table is updated during drop over
    }

    public String getTableTitle() {
        return title;
    }

    public MasterParameterSelection getSelection() {
        int[] selRows = getSelectedRows();
        ArrayList ids = new ArrayList();
        Object o;
        for (int r = 0, re = selRows.length; r < re; r++) {
            o = getValueAt(selRows[r], 0);
            if (o instanceof ReadableParameterModel)
                ids.add(((ReadableParameterModel) o).getParameterDescriptor().getId());
        }

        Integer[] arrIds = new Integer[ids.size()];
        ids.toArray(arrIds);
        try {
            return new MasterParameterSelection(deviceContext, arrIds, MasterParameterSelection.convertMasterCategoryString(category));
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        if (ParameterModelTableCellEditor.testCellEditable(e) && ParameterModelTableCellEditor.tryToggleCellAt(this, row, column))
            return false;
        return super.editCellAt(row, column, e);
    }

    public void setSelection(MasterParameterSelection ms) {
        try {
            ms.render(deviceContext.getMasterContext());
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        }
    }

    public boolean willAcceptCategory(int category) {
        int thisCat = MasterParameterSelection.convertMasterCategoryString(this.category);
        return (thisCat == category ||
                (thisCat == MasterParameterSelection.MASTER_FX_A && category == PresetParameterSelection.PRESET_FX_A) ||
                (thisCat == MasterParameterSelection.MASTER_FX_B && category == PresetParameterSelection.PRESET_FX_B)
                );
    }

    public DeviceContext getDevice() {
        return deviceContext;
    }

}
