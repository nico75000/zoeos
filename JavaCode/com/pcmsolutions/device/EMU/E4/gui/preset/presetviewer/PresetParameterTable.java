package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.parameter.SingleColumnParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.util.ArrayList;

public class PresetParameterTable extends AbstractRowHeaderedAndSectionedTable implements ZDisposable {
    protected ReadablePreset preset;
    protected String title;
    protected String category;

    public PresetParameterTable(ReadablePreset p, String category, ReadableParameterModel[] parameterModels, String title) throws ZDeviceNotRunningException {
        this(p, category, new SingleColumnParameterModelTableModel(parameterModels), title);
    }

    public PresetParameterTable(ReadablePreset p, String category, SingleColumnParameterModelTableModel tm, String title) throws ZDeviceNotRunningException {
        super(tm, null, null, /*new RowHeaderTableCellRenderer(UIColors.getVoiceOverViewTableRowHeaderSectionBG(), UIColors.getVoiceOverViewTableRowHeaderSectionFG()),*/ title + " >");
        this.preset = p;
        this.title = title;
        setDragEnabled(true);
        this.category = category;
        //this.setTransferHandler(mmth);
        this.setHidingSelectionOnFocusLost(true);
    }

    public String getCategory() {
        return category;
    }

    protected JMenuItem[] getCustomMenuItems() {
        try {
            return new JMenuItem[]{ZCommandInvocationHelper.getMenu(new Object[]{preset}, null, null, preset.getPresetDisplayName())};
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTableTitle() {
        return title;
    }

    public PresetParameterSelection getSelection() {
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
            return new PresetParameterSelection(preset, arrIds, PresetParameterSelection.convertPresetCategoryString(category));
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        } catch (PresetEmptyException e) {
            e.printStackTrace();
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
