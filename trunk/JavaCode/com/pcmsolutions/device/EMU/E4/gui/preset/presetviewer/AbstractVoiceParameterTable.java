package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnAndSectionDataProvider;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchVoiceException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Aug-2003
 * Time: 15:26:14
 * To change this template use Options | File Templates.
 */
public abstract class AbstractVoiceParameterTable extends AbstractRowHeaderedAndSectionedTable implements VoiceParameterSelectionProvider {
    protected ReadablePreset.ReadableVoice voice;
    protected String category;

    public AbstractVoiceParameterTable(ReadablePreset.ReadableVoice voice, String category, TableModel model, TransferHandler t, ColumnAndSectionDataProvider csdp, String popupName) {
        super(model, t, csdp, popupName);
        this.voice = voice;
        this.category = category;
        this.setHidingSelectionOnFocusLost(true);
    }

    public ReadablePreset.ReadableVoice getVoice() {
        return voice;
    }

    public VoiceParameterSelection getSelection() {
        int[] selRows = getSelectedRows();
        int[] selCols = getSelectedColumns();
        ArrayList ids = new ArrayList();
        Object o;
        for (int r = 0, re = selRows.length; r < re; r++)
            for (int c = 0,ce = selCols.length; c < ce; c++) {
                o = getValueAt(selRows[r], selCols[c]);
                if (o instanceof ReadableParameterModel)
                    ids.add(((ReadableParameterModel) o).getParameterDescriptor().getId());
            }

        Integer[] arrIds = new Integer[ids.size()];
        ids.toArray(arrIds);
        try {
            return new VoiceParameterSelection(voice, arrIds, VoiceParameterSelection.voiceCategoryStringToEnum(category));
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        } catch (PresetEmptyException e) {
            e.printStackTrace();
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        } catch (NoSuchVoiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ReadablePreset.ReadableVoice getReadableVoice() {
        return voice;
    }

    // String from ParameterCategories
    public String getCategory() {
        return category;
    }
}
