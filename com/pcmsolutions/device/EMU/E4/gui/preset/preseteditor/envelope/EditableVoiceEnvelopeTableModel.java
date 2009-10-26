package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope;

import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.VoiceEnvelopeTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-Jul-2003
 * Time: 00:02:53
 * To change this template use Options | File Templates.
 */
public class EditableVoiceEnvelopeTableModel extends VoiceEnvelopeTableModel {
    public EditableVoiceEnvelopeTableModel(EditableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        for (int i = 0; i < columnData.length; i++) {
            columnData[i].editor = new ParameterModelTableCellEditor(sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
            columnData[i].columnClass = EditableParameterModel.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex > 0 && columnIndex < 5 && rowIndex >= 0 && rowIndex < 3)
            return true;
        return false;
    }
}
