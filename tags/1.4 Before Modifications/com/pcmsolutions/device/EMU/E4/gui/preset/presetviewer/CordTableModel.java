package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.parameter.AbstractParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;

import javax.swing.*;

public class CordTableModel extends AbstractParameterModelTableModel {

    public CordTableModel(ReadableParameterModel[] parameterModels) {
        super(parameterModels);
        if (parameterModels.length % 3 != 0)
            throw new IllegalArgumentException("Cord parameters must be passed in sets of 3");
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 50, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[3];
        columnData[0] = new ColumnData("Source", 80, JLabel.LEFT, 0, ReadableParameterModel.class, null, null);
        columnData[1] = new ColumnData("Dest", 80, JLabel.LEFT, 0, ReadableParameterModel.class, null, null);
        columnData[2] = new ColumnData("Amt", 55, JLabel.LEFT, 0, ReadableParameterModel.class, null, null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionHeaderBG(), UIColors.getTableFirstSectionFG(), 215, "")};
    }

    protected void doRefresh() {
        for (int i = 0, n = parameterModels.length; i < n; i += 3) {
            final int j = i;
            parameterModels[j + 2].setShowUnits(true);
            tableRowObjects.add(new ColumnValueProvider() {
                private ReadableParameterModel pmSource = parameterModels[j];
                private ReadableParameterModel pmDest = parameterModels[j + 1];
                private ReadableParameterModel pmAmt = parameterModels[j + 2];

                public Object getValueAt(int col) {
                    if (col == 0)
                        return "Cord " + (j / 3);
                    else if (col == 1)
                        return pmSource;
                    else if (col == 2)
                        return pmDest;
                    else if (col == 3)
                        return pmAmt;
                    return "";
                }

                public void zDispose() {
                    pmSource = null;
                    pmDest = null;
                    pmAmt = null;
                }
            });
        }
    }

    protected void doPreRefresh() {
    }

    protected void doPostRefresh() {
    }

    protected void updateParameterModelAtIndex(int pmIndex) {
        this.fireTableCellUpdated(pmIndex / 3, pmIndex % 3 + 1);
    }
}
