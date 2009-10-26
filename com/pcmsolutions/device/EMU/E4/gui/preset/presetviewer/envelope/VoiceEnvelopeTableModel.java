package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.parameter.AbstractParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 30-Jun-2003
 * Time: 22:51:28
 * To change this template use Options | File Templates.
 */
public class VoiceEnvelopeTableModel extends AbstractParameterModelTableModel {

    public VoiceEnvelopeTableModel(ReadableParameterModel[] parameterModels) {
        super(parameterModels);
        if (parameterModels == null || parameterModels.length != 12)
            throw new IllegalArgumentException("Need exactly 12 parameters for an envelope table model");
    }

    protected void updateParameterModelAtIndex(int pmIndex) {
        this.fireTableCellUpdated(pmIndex / 4, pmIndex % 4 + 1);
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 50, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[4];
        columnData[0] = new ColumnData("Rate", 60, JLabel.LEFT, 0, ReadableParameterModel.class, null, null);
        columnData[1] = new ColumnData("Level", 60, JLabel.LEFT, 0, ReadableParameterModel.class, null, null);
        columnData[2] = new ColumnData("Rate", 60, JLabel.LEFT, 1, ReadableParameterModel.class, null, null);
        columnData[3] = new ColumnData("Level", 60, JLabel.LEFT, 1, ReadableParameterModel.class, null, null);
        sectionData = new SectionData[2];
        sectionData[0] = new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionFG(), 120, "SEG 1");
        sectionData[1] = new SectionData(UIColors.getTableSecondSectionBG(), UIColors.getTableSecondSectionFG(), 120, "SEG 2");
    }

    protected void doRefresh() {
        tableRowObjects.add(new ColumnValueProvider() {
            public void zDispose() {
            }

            public Object getValueAt(int col) {
                if (col == 0)
                    return "Attack";
                return parameterModels[col - 1];
            }
        });
        tableRowObjects.add(new ColumnValueProvider() {
            public void zDispose() {
            }

            public Object getValueAt(int col) {
                if (col == 0)
                    return "Decay";
                return parameterModels[4 + col - 1];
            }
        });
        tableRowObjects.add(new ColumnValueProvider() {
            public void zDispose() {
            }

            public Object getValueAt(int col) {
                if (col == 0)
                    return "Release";
                return parameterModels[8 + col - 1];
            }
        });
    }

    protected void doPreRefresh() {
    }

    protected void doPostRefresh() {
    }


    public void zDispose() {
        super.zDispose();
    }
}
