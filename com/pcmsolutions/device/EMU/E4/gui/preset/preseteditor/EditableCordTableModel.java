package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.CordTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 03-Jul-2003
 * Time: 01:16:22
 * To change this template use Options | File Templates.
 */
public class EditableCordTableModel extends CordTableModel {
    public EditableCordTableModel(EditableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        columnData[0].editor = new ParameterModelTableCellEditor(sectionData[columnData[0].sectionIndex].sectionBG, sectionData[columnData[0].sectionIndex].sectionFG);
        columnData[0].columnClass = EditableParameterModel.class;
        columnData[1].editor = new ParameterModelTableCellEditor(sectionData[columnData[1].sectionIndex].sectionBG, sectionData[columnData[1].sectionIndex].sectionFG);
        columnData[1].columnClass = EditableParameterModel.class;
        columnData[2].editor = new ParameterModelTableCellEditor(sectionData[columnData[2].sectionIndex].sectionBG, sectionData[columnData[2].sectionIndex].sectionFG);
        columnData[2].columnClass = EditableParameterModel.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex > 0 && columnIndex < 4 && rowIndex >= 0 && rowIndex < tableRowObjects.size())
            return true;
        return false;
    }
}
