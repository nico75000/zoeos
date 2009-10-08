package com.pcmsolutions.device.EMU.E4.gui.parameter;

import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Jul-2003
 * Time: 23:54:58
 * To change this template use Options | File Templates.
 */
public class EditableSingleColumnParameterModelTableModel extends SingleColumnParameterModelTableModel {
    public EditableSingleColumnParameterModelTableModel(EditableParameterModel[] parameterModels) {
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
