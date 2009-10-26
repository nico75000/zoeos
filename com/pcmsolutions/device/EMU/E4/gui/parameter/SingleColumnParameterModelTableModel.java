package com.pcmsolutions.device.EMU.E4.gui.parameter;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;

import javax.swing.*;

public class SingleColumnParameterModelTableModel extends AbstractParameterModelTableModel {
    public SingleColumnParameterModelTableModel(ReadableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void updateParameterModelAtIndex(int pmIndex) {
        this.fireTableCellUpdated(pmIndex, 1);
    }

    public void zDispose() {
        super.zDispose();
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 65, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[1];
        columnData[0] = new ColumnData("", 85, JLabel.LEFT, 0, ReadableParameterModel.class, null, null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionFG(), 85, "")};
    }

    protected void doPreRefresh() {

    }

    protected void doPostRefresh() {
    }

    protected void doRefresh() {
        for (int i = 0, n = parameterModels.length; i < n; i++) {
            final int j = i;
            parameterModels[j].setShowUnits(true);
            tableRowObjects.add(new ColumnValueProvider() {
                private ReadableParameterModel pm = parameterModels[j];

                public Object getValueAt(int col) {
                    if (col == 0)
                        return pm.getParameterDescriptor().getPresentationString();
                    else if (col == 1)
                        return pm;
                    return "";
                }

                public boolean equals(Object o) {
                    return pm.getParameterDescriptor().getId().equals(o);
                }

                public void zDispose() {
                    pm = null;
                }
            });
        }
    }
}
