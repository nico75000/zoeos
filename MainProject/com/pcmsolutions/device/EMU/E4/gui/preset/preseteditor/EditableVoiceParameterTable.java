package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTable;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableModel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.system.threads.Impl_ZThread;

import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Jun-2003
 * Time: 22:20:30
 * To change this template use Options | File Templates.
 */
public class EditableVoiceParameterTable extends VoiceParameterTable implements VoiceParameterSelectionAcceptor {
    protected ContextEditablePreset.EditableVoice[] voices;

    public EditableVoiceParameterTable(ContextEditablePreset.EditableVoice[] voices, String category, EditableParameterModel[] parameterModels, String title) {
        this(voices, category, new EditableVoiceParameterTableModel(parameterModels), title);
    }

    // should really take an EditableVoiceParameterTableModel
    public EditableVoiceParameterTable(ContextEditablePreset.EditableVoice[] voices, String category, VoiceParameterTableModel pgtm, String title)  {
        super(voices[0], category, pgtm, title);
        this.voices = voices;
        this.setDropChecker(new DropChecker() {
            public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                if (chosenDropFlavor instanceof VoiceParameterTableTransferHandler.VoiceParameterDataFlavor)
                    return ((VoiceParameterTableTransferHandler.VoiceParameterDataFlavor) chosenDropFlavor).containsId(((VoiceParameterTableModel) getModel()).getIdForRow(row));
                return false;
            }
        });
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
    }

    public void setSelection(final VoiceParameterSelection sel) {
/*        Integer[] ids = sel.getIds();
        Integer[] vals = sel.getVals();
        for (int i = 0,j = ids.length; i < j; i++)
            try {
                ((ContextEditablePreset.EditableVoice) voices).setVoicesParam(ids[i], vals[i]);
            } catch (DeviceException e) {
                e.printStackTrace();
            } catch (EmptyException e) {
                e.printStackTrace();
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (NoSuchVoiceException e) {
                e.printStackTrace();
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            }
            */
     //   Impl_ZThread.ddTQ.postTask(new Impl_ZThread.Task(){
       //     public void doTask() {
                sel.render(voices);
     //       }
      //  });
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        if (ParameterModelTableCellEditor.testCellEditable(e) && ParameterModelTableCellEditor.tryToggleCellAt(this, row, column))
            return false;
        return super.editCellAt(row, column, e);
    }

    public boolean willAcceptCategory(int category) {
        if (category == VoiceParameterSelection.voiceCategoryStringToEnum(this.category))
            return true;
        return false;
    }

    public ContextEditablePreset.EditableVoice getEditableVoice() {
        return (ContextEditablePreset.EditableVoice) voices[0];
    }
}
