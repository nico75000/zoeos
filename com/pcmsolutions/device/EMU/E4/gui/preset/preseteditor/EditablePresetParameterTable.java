package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.master.MasterTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.parameter.EditableSingleColumnParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter.SingleColumnParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.PresetParameterTable;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.PresetParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.device.EMU.E4.selections.MasterParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Jun-2003
 * Time: 22:20:30
 * To change this template use Options | File Templates.
 */
public class EditablePresetParameterTable extends PresetParameterTable {
    protected static PresetParameterTableTransferHandler pptth = new PresetParameterTableTransferHandler();

    public interface PresetParameterSelectionAcceptor {
        public void setSelection(PresetParameterSelection pps);
    }

    public EditablePresetParameterTable(ContextEditablePreset p, String category, EditableParameterModel[] parameterModels, String title) throws ZDeviceNotRunningException {
        this(p, category, new EditableSingleColumnParameterModelTableModel(parameterModels), title);
    }

    public EditablePresetParameterTable(ContextEditablePreset p, String category, EditableSingleColumnParameterModelTableModel tm, String title) throws ZDeviceNotRunningException {
        super(p, category, tm, title);
        this.setDropChecker(new DropChecker() {
            public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                if (chosenDropFlavor instanceof PresetParameterTableTransferHandler.PresetParameterDataFlavor)
                    return ((PresetParameterTableTransferHandler.PresetParameterDataFlavor) chosenDropFlavor).containsId(((SingleColumnParameterModelTableModel) getModel()).getIdForRow(row));
                if (chosenDropFlavor instanceof MasterTransferHandler.MasterDataFlavor)
                    return ((MasterTransferHandler.MasterDataFlavor) chosenDropFlavor).containsId(ParameterUtilities.convertPresetToMasterFxId(((SingleColumnParameterModelTableModel) getModel()).getIdForRow(row)));

                return false;
            }
        });
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
        this.setTransferHandler(pptth);
    }

    protected void setupDropOverExtent() {
        dropOverExtent = -1;
    }

    public void setSelection(PresetParameterSelection pps) {
        pps.render(((ContextEditablePreset) preset));
    }

    public void setSelection(MasterParameterSelection mps) {
        Integer[] ids = mps.getIds();
        Integer[] vals = mps.getVals();

        ArrayList l_ids = new ArrayList();
        ArrayList l_vals = new ArrayList();

        for (int i = 0; i < ids.length; i++) {
            if (ids[i].intValue() >= 228 && ids[i].intValue() <= 245) {
                l_ids.add(ParameterUtilities.convertMasterToPresetFxId(ids[i]));
                l_vals.add(vals[i]);
            }
        }
        try {
            ((ContextEditablePreset) preset).setPresetParams((Integer[]) l_ids.toArray(new Integer[l_ids.size()]), (Integer[]) l_vals.toArray(new Integer[l_vals.size()]));
        } catch (NoSuchPresetException e) {
        } catch (PresetEmptyException e) {
        } catch (IllegalParameterIdException e) {
        } catch (ParameterValueOutOfRangeException e) {
        }
    }
}

/*public MasterParameterSelection(DeviceContext dev, PresetParameterSelection pps) {
        super(dev);
        Integer[] ids = pps.getIds();
        Integer[] vals = pps.getVals();

        ArrayList l_ids = new ArrayList();
        ArrayList l_vals = new ArrayList();

        for (int i = 0; i < ids.length; i++) {
            if (ids[i].intValue() >= 6 && ids[i].intValue() <= 21) {
                l_ids.addDesktopElement(ParameterUtilities.convertPresetToMasterFxId(ids[i]));
                l_vals.addDesktopElement(vals[i]);
            }
        }
        this.category = MASTER_GENERAL;
        this.ids = (Integer[])l_ids.toArray(new Integer[l_ids.size()]);
        this.vals = (Integer[])l_vals.toArray(new Integer[l_vals.size()]);
    }*/