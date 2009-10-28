package com.pcmsolutions.device.EMU.E4.gui.master;

import com.pcmsolutions.device.EMU.E4.gui.parameter.SingleColumnParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;

public class MasterParameterTableModel extends SingleColumnParameterModelTableModel {
    public MasterParameterTableModel(EditableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        columnData[0].width += 12;
        sectionData[0].sectionWidth += 12;
        rowHeaderColumnData.width += 50;
        columnData[0].editor = new ParameterModelTableCellEditor(sectionData[columnData[0].sectionIndex].sectionBG, sectionData[columnData[0].sectionIndex].sectionFG);
        columnData[0].columnClass = EditableParameterModel.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1 && rowIndex >= 0 && rowIndex < tableRowObjects.size())
            return true;
        return false;
    }
}
