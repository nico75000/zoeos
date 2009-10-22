package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.parameter.SingleColumnParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PresetParameterTable extends AbstractRowHeaderedAndSectionedTable implements ZDisposable {
    protected ReadablePreset preset;
    protected String title;
    protected String category;

    public PresetParameterTable(ReadablePreset p, String category, ReadableParameterModel[] parameterModels, String title)  {
        this(p, category, new SingleColumnParameterModelTableModel(parameterModels), title);
    }

    public PresetParameterTable(ReadablePreset p, String category, SingleColumnParameterModelTableModel tm, String title)  {
        super(tm, null, null, /*new RowHeaderTableCellRenderer(UIColors.getVoiceOverViewTableRowHeaderSectionBG(), UIColors.getVoiceOverViewTableRowHeaderSectionFG()),*/ title + " >");
        this.preset = p;
        this.title = title;
        setDragEnabled(true);
        this.category = category;
        //this.setTransferHandler(mmth);
      //  this.setHidingSelectionOnFocusLost(true);
    }

    public String getCategory() {
        return category;
    }

    protected Component[] getCustomMenuItems() {
        try {
            return new JMenuItem[]{ZCommandFactory.getMenu(new Object[]{preset}, preset.getDisplayName())};
        } catch (PresetException e) {
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
        } catch (EmptyException e) {
            e.printStackTrace();
        } catch (ParameterException e) {
            e.printStackTrace();
        } catch (PresetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
