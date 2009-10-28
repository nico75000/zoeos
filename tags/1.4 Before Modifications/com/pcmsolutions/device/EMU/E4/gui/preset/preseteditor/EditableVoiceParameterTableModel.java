package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;

public class EditableVoiceParameterTableModel extends VoiceParameterTableModel {
    public EditableVoiceParameterTableModel(EditableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        columnData[0].editor = new ParameterModelTableCellEditor(sectionData[columnData[0].sectionIndex].sectionBG, sectionData[columnData[0].sectionIndex].sectionFG);
        columnData[0].columnClass = EditableParameterModel.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1 && rowIndex >= 0 && rowIndex < tableRowObjects.size())
            return true;
        return false;
    }
}
