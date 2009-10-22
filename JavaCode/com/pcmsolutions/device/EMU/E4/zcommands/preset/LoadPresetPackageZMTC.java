package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.gui.packaging.PackagingGUIFactory;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextEditablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.PresetPackageLoadIcon;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 29-Sep-2003
 * Time: 13:53:18
 * To change this template use Options | File Templates.
 */
public class LoadPresetPackageZMTC extends AbstractContextEditablePresetZMTCommand implements E4ContextEditablePresetZCommandMarker {

    public String getMenuPathString() {
        return ";Packaging";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        PackagingGUIFactory.loadPresetPackage(getTargets().get(0));
        return false;
    }

    public Icon getIcon() {
        return PresetPackageLoadIcon.INSTANCE;
    }

    public boolean isSuitableAsButton() {
        return true;
    }

    public String getPresentationString() {
        return "Load" + ZUtilities.DOT_POSTFIX;
    }

    public String getDescriptiveString() {
        return "Load a preset package";
    }

    public int getMaxNumTargets() {
        return 1;
    }
}
