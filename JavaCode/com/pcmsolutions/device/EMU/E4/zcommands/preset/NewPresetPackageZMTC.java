package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.gui.packaging.PackagingGUIFactory;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.PresetPackageCreateIcon;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 29-Sep-2003
 * Time: 13:53:18
 * To change this template use Options | File Templates.
 */
public class NewPresetPackageZMTC extends AbstractReadablePresetZMTCommand implements E4ReadablePresetZCommandMarker {
    public Icon getIcon() {
        return PresetPackageCreateIcon.INSTANCE;
    }

    public boolean isSuitableAsButton() {
        return true;
    }

    public String getPresentationString() {
        return "Create new" + ZUtilities.DOT_POSTFIX;
    }

    public String getDescriptiveString() {
        return "Create a new preset package";
    }

    public String getMenuPathString() {
        return ";Packaging";
    }

    public boolean handleTarget(ReadablePreset readablePreset, int total, int curr) throws Exception {
        ReadablePreset[] presets = getTargets().toArray(new ReadablePreset[numTargets()]);

        if (!PresetContextMacros.areAllSameContext(presets))
            throw new CommandFailedException("Package presets must be from the same context");

        PackagingGUIFactory.newPresetPackage(presets, suggestPackageName(presets));
        return false;
    }

    public static String suggestPackageName(ReadablePreset[] presets) throws PresetException, EmptyException {
        String[] names = new String[presets.length];
        int i = 0;
        for (ReadablePreset p : presets)
            names[i++] = p.getName();

        String suggName = ZUtilities.getCommonPrefix(names);
        if (suggName.length() < 1)
            if (presets.length == 1)
                try {
                    suggName = presets[0].getName();
                } catch (Exception e) {
                }
            else
                suggName = "Untitled";
        return suggName;
    }
}
