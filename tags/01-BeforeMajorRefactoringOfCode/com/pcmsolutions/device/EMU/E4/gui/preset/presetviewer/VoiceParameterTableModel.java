package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.parameter.SingleColumnParameterModelTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;

public class VoiceParameterTableModel extends SingleColumnParameterModelTableModel {
    public VoiceParameterTableModel(ReadableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        columnData[0].title = " ";
        sectionData[0].sectionName = "";
    }
}
