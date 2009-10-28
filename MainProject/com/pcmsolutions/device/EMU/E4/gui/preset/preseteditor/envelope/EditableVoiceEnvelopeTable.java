package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.VoiceParameterSelectionAcceptor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.VoiceEnvelopeTable;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.system.IntPool;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-Jul-2003
 * Time: 02:12:39
 * To change this template use Options | File Templates.
 */
public class EditableVoiceEnvelopeTable extends VoiceEnvelopeTable implements VoiceParameterSelectionAcceptor {
    protected ContextEditablePreset.EditableVoice[] voices;

    public EditableVoiceEnvelopeTable(ContextEditablePreset.EditableVoice[] voices, String category, EditableVoiceEnvelopeTableModel model, String title) {
        super(voices[0], category, model, title);
        this.voices = voices;
        this.setDropChecker(new DropChecker() {
            public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {

                if (chosenDropFlavor instanceof VoiceParameterTableTransferHandler.VoiceParameterDataFlavor) {
                    if (value instanceof ReadableParameterModel) {
                        int destCat = VoiceParameterSelection.voiceCategoryStringToEnum(EditableVoiceEnvelopeTable.this.category);
                        int srcCat = ((VoiceParameterTableTransferHandler.VoiceParameterDataFlavor) chosenDropFlavor).getCategory();
                        Integer id = IntPool.get(((ReadableParameterModel) value).getParameterDescriptor().getId().intValue());
                        id = ParameterUtilities.convertVoiceEnvelopeId(srcCat, destCat, id);
                        return ((VoiceParameterTableTransferHandler.VoiceParameterDataFlavor) chosenDropFlavor).containsId(id);
                    }
                }
                return false;
            }
        });
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
    }

    protected void setupDropOverExtent() {
        dropOverExtent = -1;
    }

    public void setSelection(final VoiceParameterSelection sel) {
        try {
            DeviceParameterContext dpc = voices[0].getPreset().getDeviceParameterContext();
            Integer[] ids = sel.getIds();
            Integer[] vals = sel.getVals();
            int thisCat = VoiceParameterSelection.voiceCategoryStringToEnum(category);
            int selCat = sel.getCategory();
            if (thisCat != selCat) {
                for (int i = 0, j = ids.length; i < j; i++) {
                    // need convert ids first
                    ids[i] = ParameterUtilities.convertVoiceEnvelopeId(thisCat, selCat, ids[i]);
                    vals[i] = dpc.getParameterDescriptor(ids[i]).constrainValue(vals[i]);
                }
            }
            try {
                PresetContextMacros.setContextVoicesParam(voices, ids, vals);
            } catch (PresetException e) {
                e.printStackTrace();
            }

        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    public boolean willAcceptCategory(int category) {
        if (category == VoiceParameterSelection.VOICE_AMPLIFIER_ENVELOPE
                || category == VoiceParameterSelection.VOICE_FILTER_ENVELOPE ||
                category == VoiceParameterSelection.VOICE_AUX_ENVELOPE)
            return true;
        return false;
    }

    public ContextEditablePreset.EditableVoice getEditableVoice() {
        return (ContextEditablePreset.EditableVoice) voices[0];
    }

}
