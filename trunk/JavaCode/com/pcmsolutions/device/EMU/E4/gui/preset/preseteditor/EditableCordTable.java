package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.CordTable;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.selections.CordParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.system.ZDeviceNotRunningException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Jun-2003
 * Time: 22:20:30
 * To change this template use Options | File Templates.
 */
public class EditableCordTable extends CordTable implements VoiceParameterSelectionAcceptor {
     protected ContextEditablePreset.EditableVoice[]  voices;
    public EditableCordTable(ContextEditablePreset.EditableVoice[] voices, EditableParameterModel[] parameterModels, String title) throws ZDeviceNotRunningException {
        this(voices, new EditableCordTableModel(parameterModels), title);
    }

    public EditableCordTable(ContextEditablePreset.EditableVoice[] voices, EditableCordTableModel tm, String title) throws ZDeviceNotRunningException {
        super(voices[0], tm, title);
        this.voices = voices;
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
        setDropChecker(defaultDropGridChecker);
    }

    public void setSelection(VoiceParameterSelection sel) {
        if (sel instanceof CordParameterSelection) {
            ((CordParameterSelection) sel).render(voices, getSelectedRow(), true);
        } else {
            sel.render(voices);
        }
    }

    public boolean willAcceptCategory(int category) {
        if (category == VoiceParameterSelection.VOICE_CORDS)
            return true;
        return false;
    }

    public ContextEditablePreset.EditableVoice getEditableVoice() {
        return (ContextEditablePreset.EditableVoice) voices[0];
    }
}



