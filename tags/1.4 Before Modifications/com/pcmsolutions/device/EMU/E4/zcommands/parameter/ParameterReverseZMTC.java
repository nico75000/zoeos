package com.pcmsolutions.device.EMU.E4.zcommands.parameter;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.ReverseIcon;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;
import com.pcmsolutions.system.ZCommandTargetsNotSuitableException;

import javax.swing.*;

public class ParameterReverseZMTC extends AbstractParameterZMTCommand {
    public int getMinNumTargets() {
        return 2;
    }

    public String getPresentationString() {
        return "Reverse";
    }

    public String getDescriptiveString() {
        return "Reverse values positionally";
    }

    public Icon getIcon() {
        return ReverseIcon.INSTANCE;
    }

    public boolean handleTarget(EditableParameterModel model, int total, int curr) throws Exception {
        Double[] reversedFORs = ParameterModelUtilities.getReveresedFORs((ReadableParameterModel[]) getTargets().toArray(new ReadableParameterModel[numTargets()]));
        int index = 0;
        for ( EditableParameterModel m: getTargets())
            ParameterModelUtilities.applyFORToModel(m, reversedFORs[index++]);
        return false;
    }

    public void acceptTargets() throws ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
        super.acceptTargets();
    }
}
