package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.FilterParameterTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.FilterParameterDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Jul-2003
 * Time: 22:44:43
 * To change this template use Options | File Templates.
 */
public class EditableFilterParameterTableModel extends FilterParameterTableModel {
    public EditableFilterParameterTableModel(EditableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        columnData[0].editor = new ParameterModelTableCellEditor(sectionData[columnData[0].sectionIndex].sectionBG, sectionData[columnData[0].sectionIndex].sectionFG);
        columnData[0].columnClass = EditableParameterModel.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1 && rowIndex >= 0 && rowIndex < tableRowObjects.size()) {
            Object o = getValueAt(rowIndex, columnIndex);
            if (o instanceof EditableParameterModel) {
                EditableParameterModel epm = (EditableParameterModel) o;
                if (epm.getParameterDescriptor() instanceof FilterParameterDescriptor && !((FilterParameterDescriptor) epm.getParameterDescriptor()).isCurrentlyActive())
                    return false;
            }
            return true;
        }
        return false;
    }
}
